package io.agx.bookmyseat;

import org.springframework.boot.SpringApplication;

public class TestBookMySeatApplication {

    public static void main(String[] args) {
        SpringApplication.from(BookMySeatApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
