package urlshortener2015.heatwave.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Random;

import org.springframework.http.MediaType;

import urlshortener2015.heatwave.entities.*;
import urlshortener2015.heatwave.repository.*;

public class UrlShortenerTests {

	private MockMvc mockMvc;

	@Mock
	private ShortURLRepository shortURLRepository;

	@Mock
	private ClickRepository clickRespository;

	@InjectMocks
	private MainController urlShortener;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(urlShortener).build();
	}

	@Test
	public void thatShortenerPersonalizadaAndStatisticsJson() throws Exception {
		configureTransparentSave();
		Random a = new Random();
		String nuevaPersonalizada = "";
		nuevaPersonalizada += a.nextInt(1000)+a.nextInt(1000)+a.nextInt(1000)+a.nextInt(1000)+a.nextInt(1000);
		mockMvc.perform(post("/link").param("url", "http://www.example.com/")
				.param("customTag", nuevaPersonalizada))
				.andDo(print())
				.andExpect(redirectedUrl(nuevaPersonalizada))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.hash", is(nuevaPersonalizada)))
				.andExpect(jsonPath("$.uri", is(nuevaPersonalizada)))
				.andExpect(jsonPath("$.target", is("http://www.example.com/")))
				.andExpect(jsonPath("$.sponsor", is(nullValue())));
		//Statistics with json of the shortened URL now
		mockMvc.perform(get("http://localhost/"+nuevaPersonalizada+"+")
		        .param("url", "http://example.com/").
						accept(MediaType.TEXT_HTML).
						accept(MediaType.APPLICATION_JSON));
	}

	private void configureTransparentSave() {
		when(shortURLRepository.insert(org.mockito.Matchers.any(ShortURL.class)))
				.then(new Answer<ShortURL>() {
					@Override
					public ShortURL answer(InvocationOnMock invocation) throws Throwable {
						return (ShortURL) invocation.getArguments()[0];
					}
				});
	}
}
