# Gson deserializes these via reflection using the exact field names, and Room needs the
# entity's field names to match its generated column mapping - both break silently (fields end
# up null/default instead of a build failure) if R8 renames or strips them.
-keep class com.jrom.mynextfavartist.data.entities.** {
    <fields>;
}
