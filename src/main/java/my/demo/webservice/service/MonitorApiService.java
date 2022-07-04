package my.demo.webservice.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import my.demo.webservice.common.web.Code;
import my.demo.webservice.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_ERROR;
import static org.bytedeco.ffmpeg.global.avutil.av_log_set_level;

@Service
@Slf4j
public class MonitorApiService {

    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Value("${tencent.live.key}")
    private String TENCENT_LIVE_KEY;
    @Value("${tencent.live.appName}")
    private String TENCENT_LIVE_APP_NAME;
    @Value("${tencent.live.push.domain}")
    private String TENCENT_LIVE_PUSH_DOMAIN;
    @Value("${tencent.live.push.streamName}")
    private String TENCENT_LIVE_STREAM_NAME;
    @Value("${tencent.live.pull.domain}")
    private String TENCENT_LIVE_PULL_DOMAIN;
    @Value("${tencent.live.rtsp}")
    private String TENCENT_LIVE_RTSP;
    @Value("${tencent.live.pull.key}")
    private String TENCENT_LIVE_PULL_KEY;
    @Value("${tencent.live.pull.expire}")
    private Integer TENCENT_LIVE_PULL_EXPIRE;

    // 推流过期时间 秒
    private Integer PUSH_EXPIRE_SECONDS = 12 * 60 * 60;
    // 允许最大错误数
    private Integer MAX_ERROR_COUNT = 10;

    protected FFmpegFrameGrabber grabber = null;// 解码器
    protected FFmpegFrameRecorder record = null;// 编码器
    int width;// 视频像素宽
    int height;// 视频像素高
    // 视频参数
    protected int audiocodecid;
    protected int codecid;
    protected double framerate;// 帧率
    protected int bitrate;// 比特率
    // 音频参数
    private int audioChannels;
    private int audioBitrate;
    private int sampleRate;
    private int pixelFormat;
    private PushRunnable pushRunnable = null;
    private boolean isClosing = false;

    public synchronized void startPush() {

        String rtmpUrl = genRtmpPushUrl();
        log.info("startPush rtmpUrl = {}", rtmpUrl);
        if (StrUtil.isEmpty(rtmpUrl)) {
            throw new ApiException(Code.PUSH_STREAM_ERROR, "推流地址生成失败");
        }

        try {
            if (null != record || null != grabber) {
                // 之前有推流则关闭
                close();
                // 等待完全关闭
                Thread.sleep(1000);
            }
            // 选择视频源
            from();
            // 选择输出
            to(rtmpUrl);
            // 转封装，推送到腾讯云
            go();
        } catch (Exception e) {
            log.error("推流失败：", e);
            throw new ApiException(Code.PUSH_STREAM_ERROR);
        }
    }

    public synchronized void stopPush() {
        log.info("stopPush");

        close();
    }

    /**
     * 获取监控的腾讯云直播地址
     */
    public String getLiveUrl() {
        DateTime expireTime = DateUtil.offsetSecond(DateUtil.date(), TENCENT_LIVE_PULL_EXPIRE + PUSH_EXPIRE_SECONDS);
        String safeUrl = getSafeUrl(TENCENT_LIVE_PULL_KEY, TENCENT_LIVE_STREAM_NAME, expireTime.getTime() / 1000);
        if (StrUtil.isEmpty(safeUrl)) {
            return "";
        }
        return "webrtc://" + TENCENT_LIVE_PULL_DOMAIN + "/" + TENCENT_LIVE_APP_NAME + "/" + TENCENT_LIVE_STREAM_NAME + "?" + safeUrl;
    }

    /**
     * 获取推流地址
     * 如果不传key和过期时间，将返回不含防盗链的url
     * domain 您用来推流的域名
     * streamName 您用来区别不同推流地址的唯一流名称
     * key 安全密钥
     * expireTime 过期时间 sample 2016-11-12 12:00:00
     *
     * @return String url
     */
    public String genRtmpPushUrl() {
        // 默认过期时间
        DateTime expireTime = DateUtil.offsetSecond(DateUtil.date(), PUSH_EXPIRE_SECONDS);
        String safeUrl = getSafeUrl(TENCENT_LIVE_KEY, TENCENT_LIVE_STREAM_NAME, expireTime.getTime() / 1000);
        if (StrUtil.isEmpty(safeUrl)) {
            return "";
        }
        return "rtmp://" + TENCENT_LIVE_PUSH_DOMAIN + "/" + TENCENT_LIVE_APP_NAME + "/" + TENCENT_LIVE_STREAM_NAME + "?" + safeUrl;
    }

    /**
     * 鉴权信息
     *
     * @param txTime 秒时间戳
     */
    private String getSafeUrl(String key, String streamName, long txTime) {
        String input = new StringBuilder().
                append(key).
                append(streamName).
                append(Long.toHexString(txTime).toUpperCase()).toString();

        String txSecret = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            txSecret = byteArrayToHexString(
                    messageDigest.digest(input.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return txSecret == null ? "" :
                new StringBuilder().
                        append("txSecret=").
                        append(txSecret).
                        append("&").
                        append("txTime=").
                        append(Long.toHexString(txTime).toUpperCase()).
                        toString();
    }

    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];

        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }

    /**
     * 选择视频源
     */
    private void from() throws Exception {
        grabber = new FFmpegFrameGrabber(TENCENT_LIVE_RTSP);
        FFmpegLogCallback.set();
        av_log_set_level(AV_LOG_ERROR);
        // tcp用于解决丢包问题
        grabber.setOption("rtsp_transport", "tcp");

        // 设置采集器构造超时时间
        grabber.setOption("stimeout", "2000000");
        grabber.start();// 开始之后ffmpeg会采集视频信息，之后就可以获取音视频信息
        width = grabber.getImageWidth();
        height = grabber.getImageHeight();
        // 若视频像素值为0，说明采集器构造超时，程序结束
        if (width == 0 && height == 0) {
            log.error("[ERROR]   拉流超时...");
        }
        // 视频参数
        audiocodecid = grabber.getAudioCodec();
        codecid = grabber.getVideoCodec();
        framerate = grabber.getVideoFrameRate();// 帧率
        bitrate = grabber.getVideoBitrate();// 比特率
        pixelFormat = grabber.getPixelFormat();
        sampleRate = grabber.getSampleRate();
        // 音频参数
        // 想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
        audioChannels = grabber.getAudioChannels();
        audioBitrate = grabber.getAudioBitrate();
        log.info("from audioBitrate = {}, audiocodecid = {}, audioChannels = {}", audioBitrate, audiocodecid, audioChannels);

        if (audioBitrate < 1) {
            // 设置一个未知频道，则代表不进行音频录制
            audioChannels = Integer.MAX_VALUE;
        }
    }

    /**
     * 选择输出
     */
    private void to(String rtmp) throws Exception {

        log.info("to width = {}, height = {}, framerate = {}, bitrate = {}, audioChannels = {}, audioBitrate = {}, codecid = {}, pixelFormat = {}, sampleRate = {}",
                width, height, framerate, bitrate, audioChannels, audioBitrate, codecid, pixelFormat, sampleRate);

        //未避免占用过高带宽，设置视频参数
        record = new FFmpegFrameRecorder(rtmp, width, height);
        // 这个参数的取值范围为0~51，其中0为无损模式，数值越大，画质越差，生成的文件却越小。从主观上讲，18~28是一个合理的范围。18被认为是视觉无损的（从技术角度上看当然还是有损的），它的输出视频质量和输入视频相当。
        record.setVideoOption("crf", "14");// 画面质量参数，0~51；18~28是一个合理范围
        // lossless 0为无损模式
//        record.setVideoQuality(0);
        record.setGopSize((int) framerate);
        record.setFrameRate(framerate);
        record.setVideoBitrate(bitrate);

        record.setAudioChannels(audioChannels);
        record.setAudioBitrate(audioBitrate);
        record.setSampleRate(sampleRate);

        // 编码格式; 由于rtmp不支持H265格式的视频流 (https://github.com/bytedeco/javacv/issues/1497)，为了兼容，rtsp视频流全部转码成h264的格式
        record.setFormat("flv");
        record.setAudioCodecName("aac");
        record.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        record.start();
    }

    /**
     * 转封装
     */
    private void go() throws Exception {
        pushRunnable = new PushRunnable();
        new Thread(pushRunnable).start();
    }

    private void close() {

        if (isClosing) {
            return;
        }

        isClosing = true;
        if (null != record) {
            try {
                record.close();
            } catch (FrameRecorder.Exception e) {
                log.error("record close error ", e);
            }
        }

        if (null != grabber) {
            try {
                grabber.close();
            } catch (FrameGrabber.Exception e) {
                log.error("grabber close error ", e);
            }
        }

        grabber = null;
        record = null;
        isClosing = false;
    }

    private class PushRunnable implements Runnable {

        @Override
        public void run() {
            if (null == grabber || null == record) {
                return;
            }

            int no_frame_index = 0;
            int error_index = 0;
            // 连续五次没有采集到帧则认为视频采集结束，程序错误次数超过5次即中断程序
            // 将探测时留下的数据帧释放掉，以免因为dts，pts的问题对推流造成影响
            try {
                grabber.flush();
            } catch (FrameGrabber.Exception e) {
                log.error("PushRunnable flush error ", e);
            }

            int printHasAudioTimes = 0;
            while (no_frame_index < MAX_ERROR_COUNT && error_index < MAX_ERROR_COUNT) {
                if (null == grabber || null == record) {
                    break;
                }
                try {
                    // 获取没有解码的音视频帧
                    Frame frame = grabber.grabFrame();
                    if (frame == null) {
                        log.info("go null frame");
                        // 空包记录次数跳过
                        no_frame_index++;
                        error_index++;
                        continue;
                    }

                    if (frame.getTypes().contains(Frame.Type.AUDIO) && printHasAudioTimes < 10) {
                        printHasAudioTimes++;
                        log.info("go - frame has audio, grabbed at " + grabber.getTimestamp());
                    }

                    try {
                        // 解码后重新编码
                        record.record(frame);
                    } catch (FFmpegFrameRecorder.Exception e) {
                        error_index++;
                        log.error("PushRunnable record record error", e);
                    }
                } catch (Exception e) {
                    log.error("PushRunnable error ", e);
                    // 销毁构造器
                    close();
                    break;
                }
            }

            log.info("startPush go before finish grabber = {}, record = {}, no_frame_index = {}, error_index ={}", grabber, record, no_frame_index, error_index);

            if (null != grabber && null != record && (no_frame_index >= MAX_ERROR_COUNT || error_index >= MAX_ERROR_COUNT)) {
                log.info("startPush go: max error");
                // 由于错误超出最大值的处理
                // 关闭当前线程的流处理
                close();
                // 开启新线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 重新开始推流
                        startPush();
                    }
                }).start();
                return;
            }
            // 程序正常结束销毁构造器
            log.info("startPush go finish");
            close();
        }
    }
}
