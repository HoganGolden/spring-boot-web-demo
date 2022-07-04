package my.demo.webservice.common.web.service;

import lombok.NonNull;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractCrudService<DOMAIN, ID> implements CrudService<DOMAIN> {

    private final String domainName;

    private final BaseRepository<DOMAIN, ID> repository;

    protected AbstractCrudService(BaseRepository<DOMAIN, ID> repository) {
        this.repository = repository;

        @SuppressWarnings("unchecked")
        Class<DOMAIN> domainClass = (Class<DOMAIN>) fetchType(0);
        domainName = domainClass.getSimpleName();
    }

    private Type fetchType(int index) {
        Assert.isTrue(index >= 0 && index <= 1, "type index must be between 0 to 1");

        return ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[index];
    }

    @Override
    public List<DOMAIN> listAll() {
        return repository.findAll();
    }

    @Override
    public List<DOMAIN> listAllBySpec(Specification<DOMAIN> spec) {
        return repository.findAll(spec);
    }

    @Override
    public List<DOMAIN> listAllByExample(DOMAIN domain) {
        Example<DOMAIN> example = getExample(domain);
        return repository.findAll(example);
    }

    @Override
    public List<DOMAIN> listAllByExample(DOMAIN domain, Sort sort) {
        Example<DOMAIN> example = getExample(domain);
        return repository.findAll(example, sort);
    }

    @Override
    public List<DOMAIN> listAll(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");
        return repository.findAll(sort);
    }

    @Override
    public Page<DOMAIN> listAndCountAll(@NonNull Pageable pageable) {
        Assert.notNull(pageable, "Pageable info must not be null");
        return repository.findAll(pageable);
    }

    @Override
    public Page<DOMAIN> listAndCountAll(Specification<DOMAIN> spec, @NonNull Pageable pageable) {
        Assert.notNull(pageable, "Pageable info must not be null");
        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<DOMAIN> listAndCountAll(DOMAIN domain, @NonNull Pageable pageable) {
        Assert.notNull(pageable, "Pageable info must not be null");
        Example<DOMAIN> example = getExample(domain);
        return repository.findAll(example, pageable);
    }

    @Override
    public DOMAIN getBySpec(Specification<DOMAIN> spec) {
        Optional<DOMAIN> opt = repository.findOne(spec);
        return opt.orElse(null);
    }

    @Override
    public DOMAIN getByExample(DOMAIN domain) {
        Example<DOMAIN> example = getExample(domain);
        PageRequest pageReq = PageRequest.of(0, 1);
        Page<DOMAIN> page = repository.findAll(example, pageReq);
        return page.getNumberOfElements() > 0 ? page.getContent().get(0) : null;
    }

    @Override
    public DOMAIN getByExample(DOMAIN domain, Sort sort) {
        Example<DOMAIN> example = getExample(domain);
        PageRequest pageReq = PageRequest.of(0, 1, sort);
        Page<DOMAIN> page = repository.findAll(example, pageReq);
        return page.getNumberOfElements() > 0 ? page.getContent().get(0) : null;
    }

    @Override
    public boolean exists(Specification<DOMAIN> spec) {
        Assert.notNull(spec, " spec must not be null");
        return repository.findOne(spec).isPresent();
    }

    @Override
    public boolean exists(DOMAIN domain) {
        Assert.notNull(domain, domainName + " domain must not be null");
        Example<DOMAIN> example = getExample(domain);
        return repository.exists(example);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public long countByExample(DOMAIN domain) {
        Example<DOMAIN> example = getExample(domain);
        return repository.count(example);
    }

    @Override
    public DOMAIN create(DOMAIN domain) {
        Assert.notNull(domain, domainName + " data must not be null");
        return repository.save(domain);
    }

    @Override
    public List<DOMAIN> createInBatch(Collection<DOMAIN> domains) {
        return CollectionUtils.isEmpty(domains) ? Collections.emptyList() : repository.saveAll(domains);
    }

    @Override
    public DOMAIN update(DOMAIN domain) {
        Assert.notNull(domain, domainName + " data must not be null");
        return repository.saveAndFlush(domain);
    }

    @Override
    public void flush() {
        repository.flush();
    }

    @Override
    public List<DOMAIN> updateInBatch(Collection<DOMAIN> domains) {
        return CollectionUtils.isEmpty(domains) ? Collections.emptyList() : repository.saveAll(domains);
    }

    @Override
    public void removeByExample(DOMAIN domain) {

        Example<DOMAIN> example = getExample(domain);
        List<DOMAIN> examples = repository.findAll(example);
        repository.deleteAll(examples);
    }

    @Override
    public void remove(DOMAIN domain) {
        Assert.notNull(domain, domainName + " data must not be null");
        repository.delete(domain);
    }

    @Override
    public void removeAll(Collection<DOMAIN> domains) {
        if (CollectionUtils.isEmpty(domains)) {
            return;
        }
        repository.deleteInBatch(domains);
    }

    @Override
    public void removeAll() {
        repository.deleteAll();
    }

    /**
     * 取得Example
     */
    private Example<DOMAIN> getExample(DOMAIN domain) {
        return Example.of(domain);
    }
}