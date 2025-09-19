package com.solit.sync2sing.domain.training.solo.service.impl;

import com.solit.sync2sing.domain.training.base.service.AbstractTrainingService;
import com.solit.sync2sing.domain.training.solo.service.SoloTrainingService;
import com.solit.sync2sing.global.type.TrainingMode;
import com.solit.sync2sing.repository.*;
import org.springframework.stereotype.Service;

@Service
class SoloTrainingServiceImpl extends AbstractTrainingService implements SoloTrainingService {

    public SoloTrainingServiceImpl(
            UserRepository userRepository,
            TrainingSessionRepository trainingSessionRepository,
            TrainingSessionTrainingRepository trainingSessionTrainingRepository,
            RecordingRepository recordingRepository,
            DuetTrainingRoomRepository duetTrainingRoomRepository,
            SongRepository songRepository,
            LyricslineRepository lyricslineRepository,
            DuetSongPartRepository duetSongPartRepository
    ) {
        super(
                TrainingMode.SOLO,
                userRepository,
                trainingSessionRepository,
                trainingSessionTrainingRepository,
                recordingRepository,
                duetTrainingRoomRepository,
                songRepository,
                lyricslineRepository,
                duetSongPartRepository
        );
    }

}
