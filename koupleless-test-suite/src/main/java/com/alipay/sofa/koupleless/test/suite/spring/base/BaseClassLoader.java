package com.alipay.sofa.koupleless.test.suite.spring.base;

import com.google.common.collect.Lists;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/22
 **/
public class BaseClassLoader extends URLClassLoader {

    private URLClassLoader parent;
    private URLClassLoader higherPriorityClassLoader;

    public BaseClassLoader(List<String> higherPriorityArtifacts, ClassLoader parent) {
        // add an interception layer to the parent classloader
        // in this way we can control the classloading process
        super(new URL[0], parent);
        this.parent = (URLClassLoader) parent;

        List<URL> urls = Lists.newArrayList();
        for (URL url : this.parent.getURLs()) {
            if (higherPriorityArtifacts.stream().anyMatch(url.toString()::contains)) {
                urls.add(url);
            }
        }
        this.higherPriorityClassLoader = new URLClassLoader(urls.toArray(new URL[0]));
    }

    @Override
    public URL[] getURLs() {
        return super.getURLs();
    }

    @Override
    public URL getResource(String name) {
        URL resource = higherPriorityClassLoader.getResource(name);
        resource = resource != null ? resource : super.getResource(name);
        return resource;
    }
}
