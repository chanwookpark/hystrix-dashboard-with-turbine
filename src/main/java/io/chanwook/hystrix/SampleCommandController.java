package io.chanwook.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author chanwook
 */
@Configuration
@EnableHystrix
@RestController
public class SampleCommandController {

    @RequestMapping("/h/{name}")
    public String command(@PathVariable String name) throws Exception {
        final HelloCommand command = new HelloCommand(name);
        return command.run();
    }

    @RequestMapping("/api/{name}")
    public String api(@PathVariable String name) {
        return "api::" + name;
    }

    static class HelloCommand extends HystrixCommand<String> {
        RestTemplate template = new RestTemplate();
        private final String name;

        public HelloCommand(String name) {
            super(Setter
                            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("TEST_GROUP"))
                            .andCommandKey(HystrixCommandKey.Factory.asKey("CMD::" + name))
                            .andCommandPropertiesDefaults(
                                    HystrixCommandProperties.Setter()
                                            .withCircuitBreakerEnabled(true)
                                            .withExecutionTimeoutInMilliseconds(60000)
                                            .withExecutionTimeoutEnabled(true)
                                            .withFallbackEnabled(false)
                            )
            );
            this.name = name;
        }

        @Override
        protected String run() throws Exception {
            Thread.sleep(300 * name.length());
            final String result = template.getForObject("http://localhost:7979/api/" + name, String.class);
            return "OK::" + result;
        }
    }

}
