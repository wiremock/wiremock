import {RecordingStatus} from './recording-status';

export class RecordingStatusResult {
  status: RecordingStatus;

  deserialize(unchecked: RecordingStatusResult): RecordingStatusResult {
    this.status = this.deserializeStatus(unchecked.status);
    return this;
  }

  private deserializeStatus(unchecked: any): RecordingStatus {
    switch (unchecked) {
      case 'NeverStarted':
        return RecordingStatus.NeverStarted;
      case 'Recording':
        return RecordingStatus.Recording;
      case 'Stopped':
        return RecordingStatus.Stopped;
    }
  }
}
