package org.mygovscot.decommissioned.suggest;

import org.mygovscot.decommissioned.model.Job;
import org.mygovscot.decommissioned.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;

@Controller
@RequestMapping("/redirects/suggest")
public class SuggestResource {

    @Autowired
    private SuggestService suggestService;

    @Autowired
    private JobRepository jobRepository;

    @RequestMapping(value= "{site}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Async
    public DeferredResult<Job> updateSuggestions( @RequestParam String job, @PathVariable("site") String site) throws IOException{
        Job jobObj = jobRepository.findOne(job);
        DeferredResult<Job> result = new DeferredResult<>();
        JobUpdatingSuggesterListener suggesterListener = new JobUpdatingSuggesterListener(result, jobObj, jobRepository);
        suggestService.updateSuggestions(site, suggesterListener);
        return result;
    }
}
