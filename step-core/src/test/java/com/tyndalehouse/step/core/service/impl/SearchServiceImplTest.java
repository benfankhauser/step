package com.tyndalehouse.step.core.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyndalehouse.step.core.data.DataDrivenTestExtension;
import com.tyndalehouse.step.core.data.EntityManager;
import com.tyndalehouse.step.core.data.entities.ScriptureReference;
import com.tyndalehouse.step.core.data.entities.timeline.TimelineEvent;
import com.tyndalehouse.step.core.models.search.SearchEntry;
import com.tyndalehouse.step.core.models.search.SearchResult;
import com.tyndalehouse.step.core.models.search.SubjectHeadingSearchEntry;
import com.tyndalehouse.step.core.models.search.TimelineEventSearchEntry;
import com.tyndalehouse.step.core.models.search.VerseSearchEntry;
import com.tyndalehouse.step.core.service.jsword.JSwordVersificationService;
import com.tyndalehouse.step.core.service.jsword.impl.JSwordPassageServiceImpl;
import com.tyndalehouse.step.core.service.jsword.impl.JSwordSearchServiceImpl;
import com.tyndalehouse.step.core.service.jsword.impl.JSwordVersificationServiceImpl;

/**
 * Search service testing
 * 
 * @author chrisburrell
 * 
 */
public class SearchServiceImplTest extends DataDrivenTestExtension {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceImplTest.class);
    private SearchServiceImpl si;

    /**
     * sets up each test
     */
    @Before
    public void setUp() {
        final JSwordVersificationService versificationService = new JSwordVersificationServiceImpl();
        final JSwordPassageServiceImpl jsword = new JSwordPassageServiceImpl(versificationService, null,
                null, null);
        this.si = new SearchServiceImpl(getEbean(),
                new JSwordSearchServiceImpl(versificationService, jsword), jsword, new TimelineServiceImpl(
                        getEbean(), jsword), mock(EntityManager.class));

    }

    @Test
    public void testSuggestions() {
        // si.getLexicalSuggestions(LexicalSuggestionType., form, includeAllForms)
    }

    /**
     * Random tests
     */
    @Test
    public void testMultiVersionSearch() {
        final List<SearchEntry> results = this.si.search(
                new SearchQuery("t=elijah in(ESV,KJV,ASV)", "false", 0, 1, 1)).getResults();
        assertFalse(results.isEmpty());
    }

    /** test exact strong match */
    @Test
    public void testSubjectSearch() {
        final SearchResult searchSubject = this.si.search(new SearchQuery("s=elijah in (ESV)", "false", 0, 1,
                1));

        final List<SearchEntry> entries = ((SubjectHeadingSearchEntry) searchSubject.getResults().get(0))
                .getHeadingsSearch().getResults();
        for (final SearchEntry e : entries) {
            LOGGER.debug(((VerseSearchEntry) e).getPreview());

        }
        assertTrue(searchSubject.getResults().size() > 0);
    }

    // /** test exact strong match */
    // @Test
    // public void testSearchStrong() {
    // final SearchResult searchStrong = this.si.search(new SearchQuery("o=G16 in (KJV)", "false", 0, 1, 1));
    // assertTrue("1 Peter 4:19".equals(((VerseSearchEntry) searchStrong.getResults().get(0)).getKey()));
    // }

    // /** test exact strong match */
    // @Test
    // public void testSearchRelatedStrongs() {
    // final LexiconDefinition ld = new LexiconDefinition();
    // ld.setStrong("G0016");
    // getEbean().save(ld);
    //
    // final LexiconDefinition related = new LexiconDefinition();
    // related.setStrong("G0015");
    // getEbean().save(related);
    //
    // ld.getSimilarStrongs().add(related);
    // getEbean().save(ld);
    //
    // final SearchResult searchStrong = this.si.search(new SearchQuery("o~=G16 in (KJV)", false, 0, 1, 10));
    // assertTrue(searchStrong.getResults().size() > 5);
    // }

    /** test exact strong match */
    @Test
    public void testSearchTimelineDescription() {
        // write test event to db
        final TimelineEvent e = new TimelineEvent();
        e.setName("Golden Calf episode");
        final List<ScriptureReference> references = new ArrayList<ScriptureReference>();
        final ScriptureReference sr = new ScriptureReference();
        sr.setStartVerseId(10);
        sr.setEndVerseId(15);
        references.add(sr);
        e.setReferences(references);
        getEbean().save(e);

        final SearchResult result = this.si.search(new SearchQuery("d=calf in (ESV)", "false", 0, 1, 10));
        final TimelineEventSearchEntry timelineEventSearchEntry = (TimelineEventSearchEntry) result
                .getResults().get(0);
        assertEquals("Golden Calf episode", timelineEventSearchEntry.getDescription());
        assertEquals(timelineEventSearchEntry.getVerses().get(0).getKey(), "Genesis 1:7-12");
    }
}
