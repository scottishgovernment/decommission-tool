package org.mygovscot.decommissioned.suggest;

import org.mygovscot.decommissioned.model.Job;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/redirects/suggest")
public class SuggestResource {

    @Autowired
    private SuggestService suggestService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @RequestMapping(value= "{site}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Async
    public void updateSuggestions( @RequestParam String job, @PathVariable("site") String site) throws IOException{

        Job jobObj = jobRepository.findOne(job);
        JobUpdatingSuggesterListener suggesterListener = new JobUpdatingSuggesterListener(jobObj);
        suggestService.updateSuggestions(site, suggesterListener);
    }

    private class JobUpdatingSuggesterListener implements SuggesterListener {

        private Job job;
        int pagesSeen = 0;

        public JobUpdatingSuggesterListener(Job job) {
            this.job = job;
        }

        public void start() {
            status("start");
        }

        public void processingPage(Page page) {
            pagesSeen++;
            status(pagesSeen+"/"+page.getSite().getPages().size());
        }

        public void end() {
            status("done");
        }

        private void status(String status) {
            if (job != null) {
                job.setStatus(status);
                jobRepository.save(job);
            }
        }
    }
}
