package org.jboss.narayana;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

import javax.transaction.xa.XAResource;
import java.util.Enumeration;
import java.util.Vector;

import org.jboss.narayana.DummyXAResource;

public class DummyXAResourceRecoveryHelper {

    public static void registerRecoveryResources() {
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT);
        Vector recoveryModules = manager.getModules();

        if (recoveryModules != null) {
            Enumeration modules = recoveryModules.elements();

            while (modules.hasMoreElements()) {
                RecoveryModule m = (RecoveryModule) modules.nextElement();

                if (m instanceof XARecoveryModule) {
                    XARecoveryModule  xarm = (XARecoveryModule) m;
                    xarm.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
                        public boolean initialise(String p) throws Exception {
                            return true;
                        }

                        public XAResource[] getXAResources() throws Exception {
                            return new XAResource[] {new DummyXAResource()};
                        }
                    });
                }
            }
        }
    }
}
