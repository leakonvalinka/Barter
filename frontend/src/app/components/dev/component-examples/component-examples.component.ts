import { Component } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { MapComponent } from '../../util/map/map.component';
import { MapLocation } from '../../../dtos/map-location';


@Component({
    selector: 'app-component-examples',
    imports: [MapComponent],
    templateUrl: './component-examples.component.html',
    styleUrl: './component-examples.component.scss'
})
export class ComponentExamplesComponent {

  locations: MapLocation[] = [
    {
      lat: 48.189415, 
      lng: 16.373972, 
      user: {
        displayName: "Sarah Miller",
        profilePicture: "resources/profile_icon.png",
        rating: 4.5,
        skills: ["Cleaning", "Gardening", "Pet Care", "Cooking", "Bike Repair"],
        username: "sarahmiller"
      }
    },
    {
      lat: 48.189915, 
      lng: 16.373972, 
      user: {
        displayName: "Julian Mayr",
        profilePicture: "resources/profile_icon.png",
        rating: 5,
        skills: ["Cleaning", "Gardening", "Pet Care", "Cooking", "Bike Repair"],
        username: "julianmayr"
      }
    },
  ];

  constructor(private toastr: ToastrService) { }

  showSuccess() {
    this.toastr.success('Hello world!');
  }

}
