package com.vsphere;

import com.vsphere.core.services.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

/**
 * Start application from command line:
 * java -jar <path>\vSphere-0.1.0.jar --url=<url> --username=<username> --password=<password>
 * Default variables:
 * url=10.240.3.120
 * username=Student@tn.local
 * password=12qwAZSX#E
 */
@SpringBootApplication
public class VSphereApplication implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(VSphereApplication.class);

    public static void main(String... args) {
        SpringApplication.run(VSphereApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
        logger.info("NonOptionArgs: {}", args.getNonOptionArgs());
        logger.info("OptionNames: {}", args.getOptionNames());

        for (String name : args.getOptionNames()){
            logger.info("arg-" + name + "=" + args.getOptionValues(name));
            System.setProperty(name, args.getOptionValues(name).get(0));
        }

        boolean containsOption = args.containsOption("url");
        logger.info("Contains url: " + containsOption);

        containsOption = args.containsOption("username");
        logger.info("Contains username: " + containsOption);

        containsOption = args.containsOption("password");
        logger.info("Contains password: " + containsOption);

        try{
            new Service();
        } catch (Exception e){
            System.out.println("======================================");
            System.out.println("Invalid login");
            System.out.println("======================================");
            System.exit(1);
        }
    }
}
