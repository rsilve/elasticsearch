/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.lucene.search.postingshighlight;

import org.apache.lucene.search.highlight.DefaultEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


public class CustomPassageFormatterTests {

    @Test
    public void testSimpleFormat() {
        String content = "This is a really cool highlighter. Postings highlighter gives nice snippets back. No matches here.";

        CustomPassageFormatter passageFormatter = new CustomPassageFormatter("<em>", "</em>", new DefaultEncoder());

        Passage[] passages = new Passage[3];
        String match = "highlighter";
        BytesRef matchBytesRef = new BytesRef(match);

        Passage passage1 = new Passage();
        int start = content.indexOf(match);
        int end = start + match.length();
        passage1.startOffset = 0;
        passage1.endOffset = end + 2; //lets include the whitespace at the end to make sure we trim it
        passage1.addMatch(start, end, matchBytesRef);
        passages[0] = passage1;

        Passage passage2 = new Passage();
        start = content.lastIndexOf(match);
        end = start + match.length();
        passage2.startOffset = passage1.endOffset;
        passage2.endOffset = end + 26;
        passage2.addMatch(start, end, matchBytesRef);
        passages[1] = passage2;

        Passage passage3 = new Passage();
        passage3.startOffset = passage2.endOffset;
        passage3.endOffset = content.length();
        passages[2] = passage3;

        Snippet[] fragments = passageFormatter.format(passages, content);
        assertThat(fragments, notNullValue());
        assertThat(fragments.length, equalTo(3));
        assertThat(fragments[0].getText(), equalTo("This is a really cool <em>highlighter</em>."));
        assertThat(fragments[0].isHighlighted(), equalTo(true));
        assertThat(fragments[1].getText(), equalTo("Postings <em>highlighter</em> gives nice snippets back."));
        assertThat(fragments[1].isHighlighted(), equalTo(true));
        assertThat(fragments[2].getText(), equalTo("No matches here."));
        assertThat(fragments[2].isHighlighted(), equalTo(false));
    }

    @Test
    public void testHtmlEncodeFormat() {
        String content = "<b>This is a really cool highlighter.</b> Postings highlighter gives nice snippets back.";

        CustomPassageFormatter passageFormatter = new CustomPassageFormatter("<em>", "</em>", new SimpleHTMLEncoder());

        Passage[] passages = new Passage[2];
        String match = "highlighter";
        BytesRef matchBytesRef = new BytesRef(match);

        Passage passage1 = new Passage();
        int start = content.indexOf(match);
        int end = start + match.length();
        passage1.startOffset = 0;
        passage1.endOffset = end + 6; //lets include the whitespace at the end to make sure we trim it
        passage1.addMatch(start, end, matchBytesRef);
        passages[0] = passage1;

        Passage passage2 = new Passage();
        start = content.lastIndexOf(match);
        end = start + match.length();
        passage2.startOffset = passage1.endOffset;
        passage2.endOffset = content.length();
        passage2.addMatch(start, end, matchBytesRef);
        passages[1] = passage2;

        Snippet[] fragments = passageFormatter.format(passages, content);
        assertThat(fragments, notNullValue());
        assertThat(fragments.length, equalTo(2));
        assertThat(fragments[0].getText(), equalTo("&lt;b&gt;This is a really cool <em>highlighter</em>.&lt;&#x2F;b&gt;"));
        assertThat(fragments[1].getText(), equalTo("Postings <em>highlighter</em> gives nice snippets back."));
    }
}
