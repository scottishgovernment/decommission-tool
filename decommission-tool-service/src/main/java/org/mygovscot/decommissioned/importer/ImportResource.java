package org.mygovscot.decommissioned.importer;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@Controller
@RequestMapping("/redirects/import")
public class ImportResource {

    @Inject
    private ImportService importService;

    @RequestMapping(value= "{site}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ImportResult searchByTerm( @PathVariable("site") String site, @RequestBody String csv) {
        try {
            return importService.importRedirects(site, csv);
        } catch (Throwable t) {
            throw t;
        }
    }
}
