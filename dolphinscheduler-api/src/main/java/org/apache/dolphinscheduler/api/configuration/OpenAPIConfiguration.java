/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.api.configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

/**
 * OpenAPI文档配置。基于Swagger3/OpenAPI 3.0生成API文档，提供v1和v2两个分组，
 * 并通过BeanPostProcessor修复Springfox与Spring Boot的兼容性问题。
 */
@Configuration
@ConditionalOnWebApplication
@PropertySource("classpath:swagger.properties")
public class OpenAPIConfiguration implements WebMvcConfigurer {

    /**
     * 创建V1版本API文档Docket。当前版本API，路径排除/v2/前缀。
     *
     * @return V1 Docket实例
     */
    @Bean
    public Docket createV1RestApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("v1(current)")
                .apiInfo(apiV1Info())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.apache.dolphinscheduler.api.controller"))
                .paths(PathSelectors.any().and(PathSelectors.ant("/v2/**").negate()))
                .build();
    }

    private ApiInfo apiV1Info() {
        return new ApiInfoBuilder()
                .title("Dolphin Scheduler Api Docs")
                .description("Dolphin Scheduler Api Docs")
                .version("V1")
                .build();
    }

    /**
     * 创建V2版本API文档Docket。仅包含/v2/前缀路径的API。
     *
     * @return V2 Docket实例
     */
    @Bean
    public Docket createV2RestApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("v2")
                .apiInfo(apiV2Info())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.apache.dolphinscheduler.api.controller"))
                .paths(PathSelectors.any().and(PathSelectors.ant("/v2/**")))
                .build();
    }

    private ApiInfo apiV2Info() {
        return new ApiInfoBuilder()
                .title("Dolphin Scheduler Api Docs")
                .description("Dolphin Scheduler Api Docs")
                .version("V2")
                .build();
    }

    /**
     * 创建Springfox处理器提供者后处理器。修复Springfox与Spring Boot版本兼容性问题，
     * 过滤掉使用PathPatternParser的HandlerMapping以避免启动异常。
     *
     * @return BeanPostProcessor实例
     */
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream()
                        .filter(mapping -> mapping.getPatternParser() == null)
                        .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    field.setAccessible(true);
                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
