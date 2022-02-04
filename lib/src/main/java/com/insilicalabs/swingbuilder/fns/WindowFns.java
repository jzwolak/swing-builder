package com.insilicalabs.swingbuilder.fns;

import com.insilicalabs.swingbuilder.SwingBuilderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;

import static com.insilicalabs.swingbuilder.Configurators.emptyOnClose;

public class WindowFns {
    private final static Logger LOG = LoggerFactory.getLogger(WindowFns.class);

    public static void closeAndDispose(JFrame frame) {
        SwingBuilderBase.configure(frame, emptyOnClose());
        frame.setVisible(false);
        frame.dispose();
        LOG.info("frame closed: "+frame);
    }
}
