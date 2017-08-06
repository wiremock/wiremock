import {Component, OnInit} from '@angular/core';
import {SettingsService} from './services/settings.service';
import {MessageService} from './message/message.service';
import {MdSnackBar} from '@angular/material';
import {MessageComponent} from './message/message.component';
import {UtilService} from './services/util.service';

@Component({
  selector: 'wm-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{

  isDarkTheme: boolean = false;

  constructor(private settingsService: SettingsService, private messageService: MessageService, private snackBar: MdSnackBar) { }

  ngOnInit(): void {
    this.isDarkTheme = this.settingsService.isDarkTheme();


    this.messageService.message$.subscribe(message =>{
      if(UtilService.isDefined(message)){
        this.snackBar.openFromComponent(MessageComponent, {
          duration: message.duration
        });
      }
    });
  }

  changeTheme(isDarkTheme: boolean): void{
    this.isDarkTheme = isDarkTheme;
  }
}
