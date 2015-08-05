package org.mygovscot.decommissioned.suggest;

import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(value= "{page}", method = RequestMethod.GET)
    @ResponseBody
    public int updateSuggestionsForPageDDD(@PathVariable("page") String page) throws IOException {
        return suggestService.updateSuggestions(page);
    }
}
