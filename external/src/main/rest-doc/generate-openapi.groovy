import org.hawkular.alerts.api.json.JsonUtil
import org.hawkular.alerts.api.doc.*

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.stream.Collectors

def handlersDir = new File(classesDir, "com/redhat/cloud/custompolicies/engine/handlers")
def generatedFile = new File(generatedFile)
def cl = Thread.currentThread().getContextClassLoader()
def json = [:]
def definitions = new HashSet<Class>()
def processed = new HashSet<Class>()

json["openapi"] = "3.0.0"
json["info"] = [version: alertingVersion, title: "Custom Policies Engine", description: "REST interface to Custom Policies backend engine"]
json["paths"] = [:]
json["components"] = [:]
json["components"]["schemas"] = [:]

/*
    Read handlers annotations
 */

String parseCollectionType(def typeName) {
    if (typeName == "Set" || typeName == "Collection" || typeName == "List") {
        return "array"
    } else if(typeName == "Map") {
        return "object"
    }

    return "typeErrorX"
}

def parseType(def field) {
    def cl = Thread.currentThread().getContextClassLoader()
    def fieldType
    typeJson = [:]
    if (field.type.isEnum()) {
        DocModelProperty docModelProperty = field.getDeclaredAnnotation(DocModelProperty.class)
        typeJson["type"] = "string"
        typeJson["enum"] = parseAllowableValues(docModelProperty, field)
        return typeJson
    } else if (Collection.class.isAssignableFrom(field.type) || Map.class.isAssignableFrom(field.type)) {
        ParameterizedType genericType = field.getGenericType()
        if (genericType.actualTypeArguments.length > 0) {
            def outerType = parseCollectionType(field.type.simpleName)
            typeJson["type"] = outerType
            for (def j = 0; j < genericType.actualTypeArguments.length; j++) {
                if (genericType.actualTypeArguments[j] instanceof ParameterizedType) {
                    ParameterizedType innerType = genericType.actualTypeArguments[j]
                    if (innerType.actualTypeArguments.length > 0) {
                        def innerTypeStr = parseCollectionType(field.type.simpleName)
                        def ref;

                        switch(innerTypeStr) {
                            case "array":
                                typeJson["items"] = [:]
                                ref = typeJson["items"]
                                break
                            case "object":
                                typeJson["additionalProperties"] = [:]
                                ref = typeJson["additionalProperties"]
                        }

                        for (def k = 0; k < innerType.actualTypeArguments.length; k++) {
                            Class clazz = cl.loadClass(innerType.actualTypeArguments[j].typeName)
                            if (clazz.name.startsWith("java")) {
                                ref << openApiType(clazz.simpleName)
                            } else {
                                ref["\$ref"] = '#/components/schemas/' + clazz.simpleName
                            }
                        }
                    } else {
                        System.out.println("Very odd case? " + fieldType)
                    }
                } else {
                    Class clazz = cl.loadClass(genericType.actualTypeArguments[j].typeName)
                    if (clazz.name.startsWith("java")) {
                        if(typeJson["type"] == "array") {
                            typeJson["items"] = [:]
                            typeJson["items"]["type"] = clazz.simpleName.toLowerCase()
                        }
                    } else {
                        if(typeJson["type"] == "array") {
                            typeJson["items"] = [:]
                            typeJson["items"]["\$ref"] = '#/components/schemas/' + clazz.simpleName
                        } else {
                            typeJson["additionalProperties"] = [:]
                            typeJson["additionalProperties"]["\$ref"] = '#/components/schemas/' + clazz.simpleName
                        }
                    }
                }
            }
        } else {
            System.out.println("Unsupported structure: " + field.type.simpleName)
        }
    } else {
        if (field.type.name.startsWith("com.redhat") || field.type.name.startsWith("org.hawkular")) {
            typeJson["\$ref"] = "#/components/schemas/" + field.type.simpleName
        } else {
            typeJson << openApiType(field.type.simpleName)
        }
    }
    return typeJson
}

def openApiType(def javaType) {
    type = [:]
    switch(javaType) {
        case "int":
        case "Integer":
            type["type"] = "integer"
            type["format"] = "int32"
            break
        case "long":
        case "Long":
            type["type"] = "integer"
            type["format"] = "int64"
            break
        case "String":
            type["type"] = "string"
            break
        case "Boolean":
        case "boolean":
            type["type"] = "boolean"
            break
        case "Double":
            type["type"] = "number"
            type["format"] = "double"
            break
        default:
            System.out.println("Oddball: " + javaType)
    }
    return type
}

String[] parseAllowableValues(DocModelProperty prop, Field field) {
    if (prop.allowableValues() != "") {
        return prop.allowableValues()
    }
    Class fieldClass = parseSubclass(field)
    if (fieldClass.isEnum()) {
        return Arrays.asList(fieldClass.getEnumConstants())
                .stream()
                .map { c -> c.toString() }
        .toArray()
    }
    return []
}

Class parseSubclass(def field) {
    if (field.type.isEnum() ||
            (!Collection.class.isAssignableFrom(field.type)
                    && !Map.class.isAssignableFrom(field.type))) {
        return field.type
    }

    // Get the hawkular subclass on a List, or Map supporting one level of inner collection
    def cl = Thread.currentThread().getContextClassLoader()
    ParameterizedType genericType = field.getGenericType()
    if (genericType.actualTypeArguments.length > 0) {
        for (def j = 0; j < genericType.actualTypeArguments.length; j++) {
            if (genericType.actualTypeArguments[j] instanceof ParameterizedType) {
                ParameterizedType innerType = genericType.actualTypeArguments[j]
                if (innerType.actualTypeArguments.length > 0) {
                    for (def k = 0; k < innerType.actualTypeArguments.length; k++) {
                        Class clazz = cl.loadClass(innerType.actualTypeArguments[j].typeName)
                        if (clazz.name.startsWith("com.redhat") || clazz.name.startsWith("org.hawkular")) {
                            return clazz
                        }
                    }
                }
            } else {
                Class clazz = cl.loadClass(genericType.actualTypeArguments[j].typeName)
                if (clazz.name.startsWith("com.redhat") || clazz.name.startsWith("org.hawkular")) {
                    return clazz
                }
            }
        }
    }

    return String.class
}

try {

    File[] handlers = handlersDir.listFiles()
    for (def i = 0; i < handlers.length; i++) {
        if (handlers[i].name.endsWith("Handler.class")) {
            def className = "com.redhat.cloud.custompolicies.engine.handlers." + handlers[i].name.substring(0, handlers[i].name.length() - 6)
            def clazz = cl.loadClass(className)
            def endPointPath
            if (clazz.isAnnotationPresent(DocEndpoint.class)) {
                DocEndpoint docEndpoint = clazz.getAnnotation(DocEndpoint.class)
                endPointPath = docEndpoint.value()
            }
            for (def j = 0; j < clazz.methods.length; j++) {
                Method method = clazz.methods[j]
                if (method.isAnnotationPresent(DocPath.class)) {
                    DocPath docPath = method.getAnnotation(DocPath.class)

                    def path = (endPointPath == '/' && docPath.path() != '/' ? '' : endPointPath) + (docPath.path() == '/' ? '' : docPath.path())
                    if (!json["paths"].containsKey(path)) {
                        json["paths"][path] = [:]
                    }
                    json["paths"][path][docPath.method().toLowerCase()] = [:]
                    json["paths"][path][docPath.method().toLowerCase()]["summary"] = docPath.name()
                    json["paths"][path][docPath.method().toLowerCase()]["description"] = docPath.notes()

                    if (method.isAnnotationPresent(DocParameters.class)) {
                        json["paths"][path][docPath.method().toLowerCase()]["parameters"] = []
                        DocParameters docParameters = method.getAnnotation(DocParameters.class)
                        for (def k = 0; k < docParameters.value().length; k++) {
                            DocParameter docParameter = docParameters.value()[k]
                            if(docParameter.body()) {
                                continue
                            }
                            def param = [:]
                            param["name"] = docParameter.name()
                            param["in"] = docParameter.body() ? "body" : (docParameter.path() ? "path" : "query")
                            param["description"] = docParameter.description()
                            param["required"] = docParameter.required()
                            param["schema"] = [:]
                            param["schema"] << openApiType(docParameter.type().simpleName)
                            if(docParameter.type().isEnum()) {
                                System.out.println("Enum to process, not implemented")
//                                docParameter.type().getEnumConstants()
//                                DocModelProperty docModelProperty = docParameter.type().getDeclaredAnnotation(DocModelProperty.class)
//                                param["schema"]["type"]["enum"] = "[" + parseAllowableValues(docModelProperty, docParameter.type()) + "]"
                            }

                            json["paths"][path][docPath.method().toLowerCase()]["parameters"] << param

                            definitions.add(docParameter.type())
                        }

                        for (def k = 0; k < docParameters.value().length; k++) {
                            DocParameter docParameter = docParameters.value()[k]
                            if (!docParameter.body()) {
                                continue
                            }
                            def requestBody = [:]
                            requestBody["description"] = docParameter.description()
                            requestBody["required"] = docParameter.required()
                            def content = [:]
                            content[docPath.consumes()] = [:]
                            content[docPath.consumes()]["schema"] = [:]
                            if (docParameter.type().name.startsWith("java")) {
                                content[docPath.consumes()]["schema"] << openApiType(docParameter.type().simpleName)
                            } else {
                                content[docPath.consumes()]["schema"]["\$ref"] = "#/components/schemas/" + docParameter.type().simpleName
                            }

                            requestBody["content"] = content

                            definitions.add(docParameter.type())
                            json["paths"][path][docPath.method().toLowerCase()]["requestBody"] = requestBody
                        }
                    }

                    if (method.isAnnotationPresent(DocResponses.class)) {
                        json["paths"][path][docPath.method().toLowerCase()]["responses"] = [:]
                        DocResponses docResponses = method.getAnnotation(DocResponses.class)
                        for (def k = 0; k < docResponses.value().length; k++) {
                            DocResponse docResponse = docResponses.value()[k]
                            json["paths"][path][docPath.method().toLowerCase()]["responses"]["${docResponse.code()}"] = [:]
                            json["paths"][path][docPath.method().toLowerCase()]["responses"]["${docResponse.code()}"]["description"] = docResponse.message()

                            if(docResponse.response() != DocResponse.NULL.class) {
                                def content = [:]
                                content[docPath.produces()] = [:]
                                content[docPath.produces()]["schema"] = [:]

                                def typePart = [:]

                                if (docResponse.response().name.startsWith("java")) {
                                    if (docResponse.response().simpleName == "Collection") {
                                        // Special case for findActionIds
                                        typePart["type"] = "array"
                                        typePart["items"] = [:]
                                        typePart["items"]["type"] = "string"
                                    } else {
                                        typePart << openApiType(docResponse.response().simpleName)
                                    }
                                } else {
                                    typePart["\$ref"] = "#/components/schemas/" + docResponse.response().simpleName
                                }

                                if (docResponse.responseContainer() != "") {
                                    def collectionType = [:]
                                    collectionType["type"] = parseCollectionType(docResponse.responseContainer())
                                    if (collectionType["type"] == "array") {
                                        collectionType["items"] = [:]
                                        collectionType["items"] << typePart
                                    } else {
                                        collectionType["additionalProperties"] = [:]
                                        collectionType["additionalProperties"] << typePart
                                    }
                                    content[docPath.produces()]["schema"] << collectionType
                                } else {
                                    content[docPath.produces()]["schema"] << typePart
                                }

                                json["paths"][path][docPath.method().toLowerCase()]["responses"]["${docResponse.code()}"]["content"] = content
                                definitions.add(docResponse.response())
                            }
                        }
                    }
                }
            }
        }
    }

    while (!definitions.isEmpty()) {

        def subClasses = new HashSet<Class>()
        def defIterator = definitions.iterator()
        while (defIterator.hasNext()) {
            Class clazz = defIterator.next()
            processed.add(clazz)
            if (clazz.isAnnotationPresent(DocModel.class)) {
                def className = clazz.simpleName
                if(className == "NelsonCondition") {
                    defIterator.remove()
                    continue
                }
                json["components"]["schemas"][className] = [:]
                DocModel docModel = clazz.getAnnotation(DocModel.class)
                json["components"]["schemas"][className]["description"] = docModel.description()
                json["components"]["schemas"][className]["properties"] = [:]

                Field[] fields = clazz.getDeclaredFields()
                for (def i = 0; i < fields.length; i++) {
                    if (fields[i].isAnnotationPresent(DocModelProperty.class)) {
                        DocModelProperty docModelProperty = fields[i].getDeclaredAnnotation(DocModelProperty.class)
                        def fieldName = fields[i].name
                        def fieldType = parseType(fields[i])

                        json["components"]["schemas"][className]["properties"][fieldName] = [:]
                        json["components"]["schemas"][className]["properties"][fieldName] << fieldType
                        json["components"]["schemas"][className]["properties"][fieldName]["description"] = docModelProperty.description()
                        // Perhaps pattern?
//                        json["components"]["schemas"][className]["properties"][fieldName]["allowableValues"] = allowableValues
                        if(docModelProperty.defaultValue() != "") {
                            json["components"]["schemas"][className]["properties"][fieldName]["default"] = docModelProperty.defaultValue()
                        }

                        Class subclass = parseSubclass(fields[i])
                        if (!processed.contains(subclass)) {
                            subClasses.add(subclass)
                        }
                    }
                }

                if (docModel.subTypes().length > 0) {
                    for (def i = 0; i < docModel.subTypes().length; i++) {
                        if (!processed.contains(docModel.subTypes()[i])) {
                            subClasses.add(docModel.subTypes()[i])
                        }
                    }
                }
            }
            defIterator.remove()
        }

        if (!subClasses.isEmpty()) {
            definitions.addAll(subClasses)
        }
    }

    /*
        Write output json file
     */

    generatedFile.getParentFile().mkdirs()
    JsonUtil.mapper.writeValue(generatedFile, json)

} catch (Exception e) {
    e.printStackTrace()
}

