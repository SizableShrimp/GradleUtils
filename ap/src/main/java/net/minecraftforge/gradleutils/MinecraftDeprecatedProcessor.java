/*
 * GradleUtils
 * Copyright (C) 2021 Forge Development LLC
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package net.minecraftforge.gradleutils;

import net.minecraftforge.srgutils.MinecraftVersion;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("java.lang.Deprecated")
public class MinecraftDeprecatedProcessor extends AbstractProcessor {
    private static final String DISABLE_MINECRAFT_DEPRECATED_ERROR = "disableMinecraftDeprecatedError";
    private static final String MINECRAFT_VERSION = "minecraftVersion";
    private boolean errorDisabled;
    private MinecraftVersion currentVersion;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Minecraft deprecation annotation processor enabled");
        this.errorDisabled = Boolean.parseBoolean(this.processingEnv.getOptions().get(DISABLE_MINECRAFT_DEPRECATED_ERROR));
        String minecraftVersionStr = this.processingEnv.getOptions().get(MINECRAFT_VERSION);
        if (minecraftVersionStr != null)
            this.currentVersion = MinecraftVersion.from(minecraftVersionStr);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (currentVersion == null || roundEnv.processingOver())
            return false;

        for (Element element : roundEnv.getElementsAnnotatedWith(Deprecated.class)) {
            Deprecated depAnn = element.getAnnotation(Deprecated.class);
            if (depAnn.forRemoval() && depAnn.since() != null && !depAnn.since().isEmpty()) {
                MinecraftVersion removeInVersion = MinecraftVersion.from(depAnn.since());
                if (currentVersion.compareTo(removeInVersion) >= 0) {
                    String message = "This element is deprecated for removal in " + removeInVersion + " or newer. Current minecraft version is " + currentVersion + ".";
                    this.processingEnv.getMessager().printMessage(errorDisabled ? Diagnostic.Kind.WARNING : Diagnostic.Kind.ERROR, message, element);
                }
            }
        }

        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(DISABLE_MINECRAFT_DEPRECATED_ERROR, MINECRAFT_VERSION);
    }
}
