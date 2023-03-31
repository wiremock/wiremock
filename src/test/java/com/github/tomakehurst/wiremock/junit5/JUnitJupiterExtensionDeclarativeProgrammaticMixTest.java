package com.github.tomakehurst.wiremock.junit5;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class JUnitJupiterExtensionDeclarativeProgrammaticMixTest {
    @WireMockTest
    public static class TestSaneStaticDefaults {
        @RegisterExtension
        public static WireMockExtension wms = WireMockExtension.newInstance()
            .options(wireMockConfig().port(44345))
            .build();

        @Test
        void programmatic_port_option_used_when_no_port_specified_in_attributes_static() {
            final int port = wms.getPort();
            assertThat(port, is(44345));
        }

        @Test
        void programmatic_port_is_different_from_declarative_port(WireMockRuntimeInfo wmRuntimeInfo) {
            final int declarativePort = wmRuntimeInfo.getHttpPort();
            final int staticMemberPort = wms.getPort();
            assertThat(staticMemberPort, is(not(declarativePort)));
        }

        @Test
        void wiremockruntimeinfo_always_injects_declarative_instance(WireMockRuntimeInfo wmRuntimeInfo) {
            WireMockRuntimeInfo staticMemberRuntimeInfo = wms.getRuntimeInfo();
            assertThat(wmRuntimeInfo, is(notNullValue()));
            assertThat(wmRuntimeInfo, is(not(staticMemberRuntimeInfo)));
        }
    }

    @WireMockTest(httpPort = 44777)
    public static class TestNoStaticOverride {
        @RegisterExtension
        public static WireMockExtension wms = WireMockExtension.newInstance()
            .options(wireMockConfig().port(44346))
            .build();

        @Test
        void programmatic_and_declarative_ports_are_as_defined(WireMockRuntimeInfo wmRuntimeInfo) {
            final int declarativePort = wmRuntimeInfo.getHttpPort();
            final int staticMemberPort = wms.getPort();

            assertThat(staticMemberPort, is(44346));
            assertThat(declarativePort, is(44777));
        }
    }

    @WireMockTest
    public static class TestSaneInstanceDefaults {
        @RegisterExtension
        public WireMockExtension wmi = WireMockExtension.newInstance()
            .options(wireMockConfig().port(44349))
            .build();

        @Test
        void programmatic_port_option_used_when_no_port_specified_in_attributes_instance() {
            final int port = wmi.getPort();
            assertThat(port, is(44349));
        }

        @Test
        void programmatic_port_is_different_from_declarative_port(WireMockRuntimeInfo wmRuntimeInfo) {
            final int declarativePort = wmRuntimeInfo.getHttpPort();
            final int staticMemberPort = wmi.getPort();
            assertThat(staticMemberPort, is(not(declarativePort)));
        }

        @Test
        void wiremockruntimeinfo_always_injects_declarative_instance(WireMockRuntimeInfo wmRuntimeInfo) {
            WireMockRuntimeInfo staticMemberRuntimeInfo = wmi.getRuntimeInfo();
            assertThat(wmRuntimeInfo, is(notNullValue()));
            assertThat(wmRuntimeInfo, is(not(staticMemberRuntimeInfo)));
        }
    }

    @WireMockTest(httpPort = 44778)
    public static class TestNoInstanceOverride {
        @RegisterExtension
        public WireMockExtension wmi = WireMockExtension.newInstance()
            .options(wireMockConfig().port(44351))
            .build();

        @Test
        void programmatic_port_option_used_when_no_port_specified_in_attributes_instance() {
            final int port = wmi.getPort();
            assertThat(port, is(44351));
        }

        @Test
        void programmatic_port_is_different_from_declarative_port(WireMockRuntimeInfo wmRuntimeInfo) {
            final int declarativePort = wmRuntimeInfo.getHttpPort();
            final int staticMemberPort = wmi.getPort();
            assertThat(staticMemberPort, is(not(declarativePort)));
        }

        @Test
        void wiremockruntimeinfo_always_injects_declarative_instance(WireMockRuntimeInfo wmRuntimeInfo) {
            WireMockRuntimeInfo staticMemberRuntimeInfo = wmi.getRuntimeInfo();
            assertThat(wmRuntimeInfo, is(notNullValue()));
            assertThat(wmRuntimeInfo, is(not(staticMemberRuntimeInfo)));
        }
    }
}
