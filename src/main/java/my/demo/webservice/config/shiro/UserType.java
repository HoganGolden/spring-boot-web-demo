package my.demo.webservice.config.shiro;

public enum UserType {
    CUSTOMER(0),
    ADMIN(1);

    public final Integer value;

    private UserType(Integer value) {
        this.value = value;
    }
}
