package org.vors.pairbot;

import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;

@Configuration
@EnableScheduling
public class PairbotConfig {
    @Bean(name = "freemarkerConfig")
    public freemarker.template.Configuration freemarkerConfig(){
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(new Version(2, 3, 23));

        cfg.setObjectWrapper(new Java8ObjectWrapper(freemarker.template.Configuration.VERSION_2_3_23));

        // Where do we load the templates from:
        cfg.setClassForTemplateLoading(this.getClass(), "/templates/");

        // Some other recommended settings:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return cfg;
    }
}
