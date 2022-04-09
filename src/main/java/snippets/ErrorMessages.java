package snippets;

public final class ErrorMessages {

    public static final String PLATFORM_NOT_FOUND = "The corresponding platform to your url could not be determined";
    public static final String INVALID_URL = "The given url is invalid. " +
            "The Song or Playlist or Song you want to access might be private.";
    public static final String PLAY_INVALID_URL = "The given url is invalid\n " +
            "If you want to search for a song use the /searchplay command";
    public static final String TRACK_NOT_FOUND = "The Track associated with your url could not be found";
    public static final String INTERNAL_API_ERROR = "The requested Api has some issues resolving my request";
    public static final String INTERNAL_ERROR = "An Internal Error occurred. Please contact the developers about this";
    public static final String COULD_NOT_SEND_REQUEST = "I have some issues getting the song information";
    public static final String GENERAL_ERROR = "Mmmmhhh... An error occurred :thinking:";
    public static final String INVALID_PLATFORM = "The platform that you got your link from is not supported yet";

    public static final String NOT_CONNECTED_TO_VC = "You have to be connected to a Voicechannel to use this command";
    public static final String NO_PLAYING_TRACK = "There is no track playing";
    public static final String NO_PLAYER_IN_GUILD = "There is no player registered for this guild";
    public static final String NO_PLAYER_IN_VC = "There is no player registered for this VoiceChannel";
    public static final String NO_PLAYER_FOUND = "There was no player found";
    public static final String PLAYER_NOT_FOUND = "The player for your channel wasn't found";

    public static final String INTERPRETATIONS_EMPTY = "I could not fetch any information about the song";

    public static final String INFO_GATHERING_SONG_FAILED = "Gathering information about your song failed";
    public static final String INFO_GATHERING_PLAYLIST_FAILED = "Gathering information about your playlist failed";

    public static final String LOADING_FAILED = "Loading of the given song failed";

    public static final String QUEUE_EMPTY = "Queue of the player is empty";

    public static final String BUTTON_PRESS_FAILED = "Your button press could not be processed";

    public static final String NOT_RECEIVED_PARAMETER = "Your parameter could not be received";
}
