package cz.cvut.fel.intermodal_planning.restapi;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class PlannerServerServletContextListener implements ServletContextListener {
    public final Logger log = Logger.getLogger(PlannerServerServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        log.info("Call contextInitialized(...) method.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        log.info("Call contextDestroyed(...) method.");
    }
}
