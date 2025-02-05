import { CommonModule } from "@angular/common";
import { Component } from "@angular/core";
import { RouterOutlet } from "@angular/router";

@Component({
    selector: 'app-auth-layout',
    imports: [RouterOutlet, CommonModule],
    template: `
    <div>
      <router-outlet></router-outlet>
    </div>
  `
})
export class AuthLayoutComponent {} 