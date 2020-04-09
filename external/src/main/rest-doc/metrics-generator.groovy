import org.eclipse.microprofile.metrics.annotation.Timed
import org.hawkular.alerts.api.doc.DocEndpoint
import org.hawkular.alerts.api.doc.DocPath

import java.lang.reflect.Method

def handlersDir = new File(classesDir, "com/redhat/cloud/policies/engine/handlers")
def cl = Thread.currentThread().getContextClassLoader()

try {
    def missing = new ArrayList<String>();
    File[] handlers = handlersDir.listFiles()
    for (def i = 0; i < handlers.length; i++) {
        if (handlers[i].name.endsWith("Handler.class")) {
            def className = "com.redhat.cloud.policies.engine.handlers." + handlers[i].name.substring(0, handlers[i].name.length() - 6)
            def clazz = cl.loadClass(className)
            def endPointPath
            if (clazz.isAnnotationPresent(DocEndpoint.class)) {
                DocEndpoint docEndpoint = clazz.getAnnotation(DocEndpoint.class)
                endPointPath = docEndpoint.value()
                for (def j = 0; j < clazz.methods.length; j++) {
                    Method method = clazz.methods[j]
                    if (method.isAnnotationPresent(DocPath.class)) {
                        DocPath docPath = method.getAnnotation(DocPath.class)
                        def path = (endPointPath == '/' && docPath.path() != '/' ? '' : endPointPath) + (docPath.path() == '/' ? '' : docPath.path())
                        def tags = "\"path=" + path + "\", \"method=" + docPath.method() + "\""
                        def annotation = "@Timed(absolute = true, tags = {" + tags + "})"
                        Timed existingAnnotation = method.getAnnotation(Timed.class)
                        if (existingAnnotation == null) {
                            missing.add(sprintf("Class: %s, Method: %s, annotation: %s\n", className, method.getName(), annotation))
                        }
                    }
                }
            }
        }
    }

    if(missing.size() > 0) {
        println("Following handler methods are missing timed annotations:")
        missing.each{m ->
            println(m)
        }
    }
} catch (Exception e) {
    e.printStackTrace()
}