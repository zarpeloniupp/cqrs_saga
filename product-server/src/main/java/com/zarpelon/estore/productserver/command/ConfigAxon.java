package com.zarpelon.estore.productserver.command;

import com.thoughtworks.xstream.XStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigAxon {

    @Bean
    public XStream xStream() {
        XStream xStream = new XStream();

        xStream.allowTypesByWildcard(new String[] { "com.zarpelon.estore.productserver.**", "com.appsdeveloperblog.estore.core.**" });
        return xStream;
    }

}
