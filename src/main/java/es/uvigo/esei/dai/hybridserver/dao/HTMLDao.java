package es.uvigo.esei.dai.hybridserver.dao;

import java.util.Set;

public interface HTMLDao {
    void savePage(String uuid, String htmlContent) throws Exception;
    String getPage(String uuid) throws Exception;
    boolean deletePage(String uuid) throws Exception;
    Set<String> listPages() throws Exception;
}
