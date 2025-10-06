package es.uvigo.esei.dai.hybridserver;

import es.uvigo.esei.dai.hybridserver.dao.HTMLDao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HTMLController {
    private final HTMLDao dao;

    public HTMLController(HTMLDao dao) {
        this.dao = dao;
    }

    public void createPage(String uuid, String content) throws Exception {
        dao.savePage(uuid, content);
    }

    public String getPage(String uuid) throws Exception {
        return dao.getPage(uuid);
    }

    public boolean deletePage(String uuid) throws Exception {
        return dao.deletePage(uuid);
    }

    public Map<String,String> listPages() throws Exception {
        Map<String,String> result = new HashMap<>();
        Set<String> uuids = dao.listPages();
        for(String uuid : uuids) {
            String content = dao.getPage(uuid);
            if(content != null)
                result.put(uuid, content);
        }
        return result;
    }
}
