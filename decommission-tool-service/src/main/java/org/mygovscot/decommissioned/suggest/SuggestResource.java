package org.mygovscot.decommissioned.suggest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/redirects/suggest")
public class SuggestResource {

    @Autowired
    private SuggestService suggestService;

    @RequestMapping(value= "{site}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SuggestResults updateSuggestions( @PathVariable("site") String site) throws IOException{
        return suggestService.updateSuggestions(site);
    }
}
