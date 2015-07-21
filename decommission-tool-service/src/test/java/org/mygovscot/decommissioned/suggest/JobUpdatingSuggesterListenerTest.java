package org.mygovscot.decommissioned.suggest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mygovscot.decommissioned.model.Job;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;

@ContextConfiguration(classes=JobUpdatingSuggesterListenerTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class JobUpdatingSuggesterListenerTest {

    @Autowired
    private JobRepository jobRepository;


//    @Test
//    public void ignoresNullJob() {
//        JobUpdatingSuggesterListener sut = new JobUpdatingSuggesterListener(null, jobRepository);
//        sut.start();
//        sut.processingPage(anyPage());
//        sut.end();
//    }
//
//    @Test
//    public void updatesJob() {
//        Job job = new Job();
//        job.setId("found");
//        JobUpdatingSuggesterListener sut = new JobUpdatingSuggesterListener(job, jobRepository);
//        sut.start();
//        sut.processingPage(anyPage());
//        sut.end();
//    }
//
//    private Page anyPage() {
//        Site site = new Site();
//        Page page = new Page();
//        site.setPages(new ArrayList<>());
//        site.getPages().add(page);
//        page.setSite(site);
//        return page;
//    }
//
//    @Bean
//    private JobRepository getJobRepository() {
//        JobRepository jr = Mockito.mock(JobRepository.class);
//        Mockito.when(jr.findOne("found")).thenReturn(new Job());
//        return jr;
//    }
}
