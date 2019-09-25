package anlin.softdev.kuleuven.memories;

import android.content.SearchRecentSuggestionsProvider;

public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "anlin.softdev.kuleuven.memories.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
