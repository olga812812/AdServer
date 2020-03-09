package adserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc
public class AdServerApplicationSmokeTests {
    @Autowired
    private MockMvc mvc;


    @Test
    public void checkRespCode200() throws  Exception {
       mvc.perform(get("/puid30=5318")).andExpect(status().isOk()).andExpect(content().string(containsString("You got text.xml file")));
    }

    @Test
    public void checkDefaultResp() throws  Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("AdID=\"empty\"")));
    }

    @Test
    public void checkRespCode204() throws  Exception {
        mvc.perform(get("/test?var=code204")).andExpect(status().isNoContent()).andExpect(content().string(""));
    }

    @Test
    public void checkRespCode302() throws  Exception {
        mvc.perform(get("/code302/next/path?and=param")).andExpect(status().is(302)).andExpect(header().string("Location", equalTo("http://youReceivedRedirectUrl.ru")));
    }

    @Test
    public void checkCorsHeaders() throws  Exception {
       mvc.perform(get("/test?var=code204").header("Origin", "OriginHeaderValue")).andExpect(status().isNoContent())
                .andExpect(header().string("Access-Control-Allow-Origin", "OriginHeaderValue"))
                        .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

}
