package es.uvigo.esei.dai.hybridserver.dao;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HTMLMapDao implements HTMLDao {
    private final Map<String,String> pages = new ConcurrentHashMap<>();

    @Override
    public void savePage(String uuid, String content) { pages.put(uuid, content); }

    @Override
    public String getPage(String uuid) { return pages.get(uuid); }

    @Override
    public boolean deletePage(String uuid) { return pages.remove(uuid) != null; }

    @Override
    public Set<String> listPages() { return pages.keySet(); }
}


