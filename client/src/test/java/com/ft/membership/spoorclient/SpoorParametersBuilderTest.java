package com.ft.membership.spoorclient;

import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpoorParametersBuilderTest {
    @Test
    public void shouldParseParametersFromRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getCookies())
                .thenReturn(
                        new Cookie[]{
                                new Cookie("FTSession", "aFTSessionCookie"),
                                new Cookie("spoor-id", "aSpoorIdCookie"),
                        }
                );

        when(request.getHeader("User-Agent")).thenReturn("aUserAgentHeader");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://appserver-not-approot/contextpath"));
        when(request.getQueryString()).thenReturn("query=param");

        DefaultSpoorParametersBuilder trackingParametersFactory = new DefaultSpoorParametersBuilder(
                "anApiKey",
                "https://approot.com",
                "aProduct");
        DefaultSpoorParameters trackingParameters = trackingParametersFactory.fromRequest(request, of("aRootId")).build();

        assertThat(trackingParameters.getAction()).isEqualTo("view");
        assertThat(trackingParameters.getCategory()).isEqualTo("page");
        assertThat(trackingParameters.getContext().getId()).isNotEmpty();
        assertThat(trackingParameters.getContext().getProduct()).isEqualTo("aProduct");
        assertThat(trackingParameters.getContext().getRootId()).isEqualTo(empty());
        assertThat(trackingParameters.getContext().getRootId()).isEqualTo(empty());
        assertThat(trackingParameters.getContext().getUrl()).isEqualTo("https://approot.com/contextpath?query=param");
        assertThat(trackingParameters.getDevice().getSpoorId()).isEqualTo(of("aSpoorIdCookie"));
        assertThat(trackingParameters.getUser().getFtSession()).isEqualTo(of("aFTSessionCookie"));
        assertThat(trackingParameters.getDevice().getUserAgent()).isEqualTo(of("aUserAgentHeader"));
    }

    @Test
    public void shouldHandleMissingValues() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getCookies()).thenReturn(new Cookie[]{});
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://appserver-not-approot/contextpath"));

        DefaultSpoorParametersBuilder trackingParametersFactory = new DefaultSpoorParametersBuilder(
                "anApiKey",
                "https://approot.com",
                "aProduct");

        DefaultSpoorParameters trackingParameters = trackingParametersFactory.fromRequest(request, of("aRootId")).build();

        assertThat(trackingParameters.getContext().getId()).isNotEmpty();
        assertThat(trackingParameters.getDevice().getSpoorId()).isEqualTo(Optional.empty());
        assertThat(trackingParameters.getDevice().getSpoorSession()).isEqualTo(Optional.empty());
        assertThat(trackingParameters.getDevice().getUserAgent()).isEqualTo(Optional.empty());
    }

    @Test
    public void buildPageView() {
        DefaultSpoorParametersBuilder trackingParametersFactory = new DefaultSpoorParametersBuilder(
                "anApiKey",
                "https://approot.com",
                "aProduct");

        DefaultSpoorParameters trackingParameters = trackingParametersFactory.pageView().build();

        assertThat(trackingParameters.getAction()).isEqualTo("view");
        assertThat(trackingParameters.getCategory()).isEqualTo("page");
    }

    @Test
    public void buildEvent() {
        DefaultSpoorParametersBuilder trackingParametersFactory = new DefaultSpoorParametersBuilder(
                "anApiKey",
                "https://approot.com",
                "aProduct");

        DefaultSpoorParameters trackingParameters = trackingParametersFactory.event("foo", "blah").build();

        assertThat(trackingParameters.getAction()).isEqualTo("foo");
        assertThat(trackingParameters.getCategory()).isEqualTo("blah");
    }

    @Test
    public void buildFunnel() {
        DefaultSpoorParametersBuilder trackingParametersFactory = new DefaultSpoorParametersBuilder(
                "anApiKey",
                "https://approot.com",
                "aProduct");

        final SpoorFunnelStepData funnelStepData = new SpoorFunnelStepData("Funnel1", 1, "Step1", 1);
        DefaultSpoorParameters trackingParameters = trackingParametersFactory.funnel(funnelStepData).build();

        assertThat(trackingParameters.getContext().getFunnel().get()).isEqualTo(funnelStepData);
    }
}