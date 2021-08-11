package org.mygovscot.decommissioned.bulk;

import org.mygovscot.decommissioned.repository.PageRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Controller
@RequestMapping("/redirects/bulk")
public class BulkResource {

    @Inject
    private PageRepository pageRepository;

    @RequestMapping( value= "/lock", method = RequestMethod.PUT)
    @ResponseBody
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void setLock( @RequestBody BulkRequest request) {
        pageRepository.bulkSetLock(request.getIds(), request.isFlag());
    }

    @RequestMapping( value= "/delete", method = RequestMethod.PUT)
    @ResponseBody
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void delete( @RequestBody BulkRequest request) {
        pageRepository.bulkDelete(request.getIds());
    }

    @RequestMapping( value= "/settarget", method = RequestMethod.PUT)
    @ResponseBody
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void setTaget( @RequestBody BulkRequest request) {
        pageRepository.bulkSetTarget(request.getIds(), request.getTargetUrl());
    }
}

