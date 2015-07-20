package org.mygovscot.decommissioned.suggest;

import org.mygovscot.decommissioned.model.Job;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.repository.JobRepository;
import org.springframework.web.context.request.async.DeferredResult;

class JobUpdatingSuggesterListener implements SuggesterListener {

    private DeferredResult<Job> result;
    private JobRepository jobRepository;

    private Job job;
    int pagesSeen = 0;

    public JobUpdatingSuggesterListener(DeferredResult<Job> result, Job job, JobRepository jobRepository) {
        this.result = result;
        this.job = job;
        this.jobRepository = jobRepository;
    }

    public void start() {
        status("start");
    }

    public void processingPage(Page page) {
        pagesSeen++;
        status(pagesSeen + "/" + page.getSite().getPages().size());
    }

    public void end() {
        status("done");
        result.setResult(job);
    }

    private void status(String status) {
        if (job != null) {
            job.setStatus(status);
            jobRepository.save(job);
        }
    }
}
