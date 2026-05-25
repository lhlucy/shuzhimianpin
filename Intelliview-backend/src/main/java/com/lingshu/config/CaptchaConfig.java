//????????
//package com.lingshu.config;
//
//import com.google.code.kaptcha.Producer;
//import com.google.code.kaptcha.impl.DefaultKaptcha;
//import com.google.code.kaptcha.util.Config;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Properties;
//
//@Configuration
//public class CaptchaConfig {
//
//    @Bean
//    public Producer captchaProducer() {
//        Properties properties = new Properties();
//        properties.setProperty("kaptcha.border", "yes");
//        properties.setProperty("kaptcha.border.color", "105,179,90");
//        properties.setProperty("kaptcha.textproducer.font.color", "blue");
//        properties.setProperty("kaptcha.image.width", "125");
//        properties.setProperty("kaptcha.image.height", "50");
//        properties.setProperty("kaptcha.textproducer.font.size", "40");
//        properties.setProperty("kaptcha.session.key", "code");
//        properties.setProperty("kaptcha.textproducer.char.length", "4");
//        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
//        properties.setProperty("kaptcha.noise.color", "black");
//        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");
//        properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple");
//        properties.setProperty("kaptcha.background.clear.from", "240,240,240");
//        properties.setProperty("kaptcha.background.clear.to", "255,255,255");
//
//        Config config = new Config(properties);
//        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
//        defaultKaptcha.setConfig(config);
//
//        return defaultKaptcha;
//    }
//}

