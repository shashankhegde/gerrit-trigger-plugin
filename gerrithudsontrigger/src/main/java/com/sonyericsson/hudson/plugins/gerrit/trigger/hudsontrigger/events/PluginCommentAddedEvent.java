/*
 *  The MIT License
 *
 *  Copyright 2012 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.events;

import static com.sonyericsson.hudson.plugins.gerrit.trigger.GerritServer.ANY_SERVER;

import com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.events.CommentAdded;
import com.sonyericsson.hudson.plugins.gerrit.trigger.GerritServer;
import com.sonyericsson.hudson.plugins.gerrit.trigger.Messages;
import com.sonyericsson.hudson.plugins.gerrit.trigger.VerdictCategory;
import com.sonyericsson.hudson.plugins.gerrit.trigger.PluginImpl;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.util.ListBoxModel;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event configuration that causes the build to be triggered when a comment is added.
 * @author Tomas Westling &lt;tomas.westling@sonymobile.com&gt;
 */
public class PluginCommentAddedEvent extends PluginGerritEvent implements Serializable {
    private static final long serialVersionUID = -1190562081236235819L;
    private String verdictCategory;
    private String commentAddedTriggerApprovalValue;
    private String commentPattern;

    private static final Logger logger = LoggerFactory.getLogger(PluginGerritEvent.class);

    /**
     * Standard DataBoundConstructor.
     * @param verdictCategory the value part of the VerdictCategory.
     * @param commentAddedTriggerApprovalValue the approval value.
     */
    @DataBoundConstructor
    public PluginCommentAddedEvent(String verdictCategory,
                                   String commentAddedTriggerApprovalValue,
                                   String commentPattern ) {

        logger.trace("Category: {}", verdictCategory);
        logger.trace("Approval value: {}", commentAddedTriggerApprovalValue);
        logger.trace("Pattern: {}", commentPattern);
        this.verdictCategory = verdictCategory;
        this.commentAddedTriggerApprovalValue = commentAddedTriggerApprovalValue;
        this.commentPattern = commentPattern;
        /*
        if (commentPattern) {
            this.commentPattern = Pattern.compile(commentPattern);
        else {
            this.commentPattern = Pattern.compile(".*");
        }
        */
        
    }

    /**
     * Empty constructor for serializer.
     */
    public PluginCommentAddedEvent() {
    }

    /**
     * Getter for the commentAddedTriggerApprovalValue.
     * @return the value.
     */
    public String getCommentAddedTriggerApprovalValue() {
        return commentAddedTriggerApprovalValue;
    }

    /**
     * Getter for the verdictCategory.
     * @return the verdictCategory.
     */
    public String getVerdictCategory() {
        return verdictCategory;
    }

    public boolean matchComment(String comment) {
        logger.trace("Comment: {}", comment);
        Pattern pattern = Pattern.compile(commentPattern);
        Matcher m = pattern.matcher(comment);
        if (m.find()) {
            logger.trace("Pattern found!");
            return true;
        }
        logger.trace("Pattern not found");
        return false;
    }

    /**
     * Getter for the Descriptor.
     * @return the Descriptor for the PluginCommentAddedEvent.
     */
    public Descriptor<PluginGerritEvent> getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(PluginCommentAddedEventDescriptor.class);
    }

    @Override
    public Class getCorrespondingEventClass() {
        return CommentAdded.class;
    }

    /**
     * The Descriptor for the PluginCommentAddedEvent.
     */
    @Extension
    public static class PluginCommentAddedEventDescriptor extends PluginGerritEventDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.CommentAddedDisplayName();
        }

        /**
         * Fills the verdict category drop-down list.
         *
         * @param serverName the name of the server selected in the "Choose Server" dropdown.
         * @return a ListBoxModel for the drop-down list.
         */
        public ListBoxModel doFillVerdictCategoryItems(
                    @QueryParameter("serverName") @RelativePath(value = "..") String serverName) {
            ListBoxModel m = new ListBoxModel();

            Collection<VerdictCategory> list = null;
            if (ANY_SERVER.equals(serverName)) { //list all configured VCs in all servers
                Map<String, VerdictCategory> map = new HashMap<String, VerdictCategory>();
                for (GerritServer server : PluginImpl.getInstance().getServers()) {
                    for (VerdictCategory vc : server.getConfig().getCategories()) {
                        if (!map.containsKey(vc.getVerdictValue())) {
                            map.put(vc.getVerdictValue(), vc);
                        }
                    }
                }
                list = map.values();
            } else {
                list = PluginImpl.getInstance().getServer(serverName).getConfig().getCategories();
            }
            if (list != null && !list.isEmpty()) {
                for (VerdictCategory v : list) {
                    m.add(v.getVerdictDescription(), v.getVerdictValue());
                }
            }
            return m;
        }
    }

}
