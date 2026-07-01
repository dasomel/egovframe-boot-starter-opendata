package org.egovframe.boot.opendata.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.egovframe.boot.opendata.client.EgovOpenApiClient;
import static org.assertj.core.api.Assertions.assertThat;

class EgovOpenApiAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EgovOpenApiAutoConfiguration.class))
            .withPropertyValues("egovframe.opendata.base-url=https://apis.data.go.kr",
                    "egovframe.opendata.service-key=test");

    @Test void registersClientBean() {
        runner.run(ctx -> assertThat(ctx).hasSingleBean(EgovOpenApiClient.class));
    }
}
