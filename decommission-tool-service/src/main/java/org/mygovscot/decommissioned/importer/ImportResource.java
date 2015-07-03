package org.mygovscot.decommissioned.importer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/import")
public class ImportResource {

    @Autowired
    private ImportService importService;

    @RequestMapping(value= "{site}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ImportResult searchByTerm( @PathVariable("site") String site, @RequestBody String csv) {
        return importService.importRedirects(site, csv);
    }
}
