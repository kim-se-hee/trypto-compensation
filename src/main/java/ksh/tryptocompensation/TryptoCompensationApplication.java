package ksh.tryptocompensation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TryptoCompensationApplication {
    public static void main(String[] args) {
        SpringApplication.run(TryptoCompensationApplication.class, args);
    }
}
