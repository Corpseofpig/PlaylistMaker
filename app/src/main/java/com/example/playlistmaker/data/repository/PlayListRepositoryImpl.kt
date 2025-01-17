package com.example.playlistmaker.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import com.example.playlistmaker.data.db.playlist.PlayListEntity
import com.example.playlistmaker.data.db.playlist.PlayListsDatabase
import com.example.playlistmaker.data.db.playlist.TrackInPlaylistEntity
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlayListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.sql.Time
import java.util.Date

class PlayListRepositoryImpl(private val database: PlayListsDatabase): PlayListRepository {
    override suspend fun insertPlayList(playList: PlayList) {
        database.playlistDao().insertPlayList(playList.fromDomainModel())
    }

    override suspend fun deletePlayList(playList: PlayList) {
        database.playlistDao().deletePlayList(playList.fromDomainModel())
    }

    override suspend fun insertTrackToPlaylist(playList: PlayList, track: Track) {
        val trackId = track.trackId.toLong()
        if (playList.tracksId.contains(trackId)) {
            return
        }
        playList.tracksId.add(trackId)
        playList.trackCount ++
        database.playlistDao().updatePlaylist(playList.fromDomainModel())
        database.playlistDao().insertTrackToPlaylist(track.fromDomainModel())
    }

    override suspend fun getAllPlaylists(): Flow<List<PlayList>>  = flow {
        emit(database.playlistDao().getAllPlaylists().map {it.toDomainModel()})
    }

    override suspend fun getPlaylistById(id: Int): PlayList {
         return database.playlistDao().getPlaylistById(id).toDomainModel()
    }

    override suspend fun saveImageToStorage(context: Context, uri: Uri): Uri? {
        val path = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PlaylistCovers")
        if (!path.exists()){
            path.mkdirs()
        }
        val file = File(path, "playlist_${Date().time.toString()}.jpg")
        val input = context.contentResolver.openInputStream(uri)
        val output = FileOutputStream(file)
        BitmapFactory
            .decodeStream(input)
            .compress(Bitmap.CompressFormat.JPEG, 40, output)
        val outputUri = Uri.fromFile(file)
        return outputUri
    }

}

    fun PlayList.fromDomainModel(): PlayListEntity {
        return PlayListEntity(
            this.id,
            this.name,
            this.description,
            this.imageTitle,
            this.tracksId,
            this.trackCount,
            this.imageUri
        )

    }

    fun PlayListEntity.toDomainModel(): PlayList {
        return PlayList(
            this.id,
            this.name,
            this.description,
            this.imageTitle,
            this.tracksId,
            this.trackCount,
            this.imageUri
        )
    }

    fun Track.fromDomainModel(): TrackInPlaylistEntity {
        return TrackInPlaylistEntity(
            this.trackId,
            this.trackName,
            this.artistName,
            this.trackTimeMillis,
            this.artworkUrl100,
            this.collectionName,
            this.releaseDate,
            this.primaryGenreName,
            this.country,
            this.previewUrl,
            Date().time
        )
    }

    fun TrackInPlaylistEntity.fromDomainModel(): Track {
        return Track(
            this.trackId,
            this.trackName,
            this.artistName,
            this.trackTimeMillis,
            this.artworkUrl100,
            this.collectionName,
            this.releaseDate,
            this.primaryGenreName,
            this.country,
            this.previewUrl
        )
    }

