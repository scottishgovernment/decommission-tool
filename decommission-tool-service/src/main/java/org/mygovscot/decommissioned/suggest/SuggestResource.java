package org.mygovscot.decommissioned.suggest;

import org.mygovscot.decommissioned.model.Job;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/redirects/suggest")
public class SuggestResource {

    private static final Logger LOG = LoggerFactory.getLogger(SuggestResource.class);

    @Autowired
    private SuggestService suggestService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @RequestMapping(value= "{site}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Job updateSuggestions( @PathVariable("site") String site) throws IOException{
        Job job = new Job();
        JobUpdatingSuggesterListener suggesterListener = new JobUpdatingSuggesterListener(job);
        job.setStatus("start");
        jobRepository.save(job);

        taskExecutor.submit(new Runnable() {
            @Override
            public void run(){
                try {
                    suggestService.updateSuggestions(site, suggesterListener);
                } catch (IOException e) {
                    job.setStatus("error:" + e.getMessage());
                    jobRepository.save(job);
                    LOG.error("Excpetion generating suggestions", e);
                }
            }
        });
        return job;
    }

    private class JobUpdatingSuggesterListener implements SuggesterListener {

        private Job job;
        int pagesSeen = 0;

        public JobUpdatingSuggesterListener(Job job) {
            this.job = job;
        }

        public void start() {
            // nothing requred
        }

        public void processingPage(Page page) {
            pagesSeen++;
            job.setStatus(pagesSeen+"/"+page.getSite().getPages().size());
            jobRepository.save(job);
        }

        public void end() {
            job.setStatus("done");
            jobRepository.save(job);
        }
    }
}
