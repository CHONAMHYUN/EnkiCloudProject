package spring.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebTestControlAdd {
    @RequestMapping("/addControlTest")
    public ModelAndView webtestadd() {

        String message = "this is a test for adding control on spring framework";
        return new ModelAndView("addControlTest", "addspring", message); 
    }

}
