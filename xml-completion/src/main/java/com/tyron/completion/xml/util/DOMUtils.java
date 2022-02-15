package com.tyron.completion.xml.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tyron.completion.xml.repository.api.ResourceNamespace;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.w3c.dom.NamedNodeMap;

import java.util.List;

public class DOMUtils {

    public static String lookupPrefix(DOMAttr attr) {
        return lookupPrefix(attr, getPrefix(attr));
    }

    public static String lookupPrefix(DOMAttr attr, String prefix) {
        DOMElement element = attr.getOwnerElement();
        while (element != null) {
            List<DOMAttr> nodes = element.getAttributeNodes();
            if (nodes != null) {
                for (DOMAttr node : nodes) {
                    if (!node.isXmlns()) {
                        continue;
                    }

                    if (prefix.equals(node.getLocalName())) {
                        return node.getValue();
                    }
                }
            }

            element = element.getParentElement();
        }
        return prefix;
    }

    public static String getPrefix(DOMAttr attr) {
        String name = attr.getName();
        if (!name.contains(":")) {
            return name;
        }
        return name.substring(0, name.indexOf(':'));
    }

    @Nullable
    public static DOMElement getRootElement(DOMDocument document) {
        List<DOMNode> roots = document.getRoots();
        for (DOMNode root : roots) {
            if (root instanceof DOMElement) {
                return (DOMElement) root;
            }
        }
        return null;
    }

    public static ResourceNamespace.Resolver getNamespaceResolver(DOMDocument document) {
        DOMElement rootElement = getRootElement(document);
        if (rootElement == null) {
            return ResourceNamespace.Resolver.EMPTY_RESOLVER;
        }

        return new ResourceNamespace.Resolver() {

            @Nullable
            @Override
            public String uriToPrefix(@NonNull String namespaceUri) {
                return rootElement.getPrefix(namespaceUri);
            }

            @Nullable
            @Override
            public String prefixToUri(@NonNull String namespacePrefix) {
                DOMAttr xmlns = rootElement.getAttributeNode("xmlns", namespacePrefix);
                if (xmlns != null) {
                    return xmlns.getValue();
                }
                return null;
            }
        };
    }
}