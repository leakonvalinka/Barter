import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateUserRating } from '../../../dtos/rating';
import {CommonModule} from '@angular/common';
import { BarteringService } from '../../../services/bartering/bartering.service';
import { ToastrService } from 'ngx-toastr';
import { RatingService } from '../../../services/rating/rating.service';

@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './rating.component.html',
  styleUrl: './rating.component.scss'
})
export class RatingComponent implements OnInit{
  @Input() exchangeId!: number;
  @Input() exchangeCompleted = false;
  @Output() ratingSubmitted = new EventEmitter<void>();

  ratingForm!: FormGroup;
  stars = [0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5];
  currentRating = 0;
  hoverRating = 0;
  isSubmitted = false;

  showRatingForm = false;

  constructor(
    private fb: FormBuilder,
    private barteringService: BarteringService,
    private toastr: ToastrService,
  ) {}

  ngOnInit(): void {
    this.checkIfExchangeRatable();
    this.initForm();
  }

  private checkIfExchangeRatable(): void {
    this.barteringService.getSkillExchangeByID(this.exchangeId).subscribe({
      next: (exchangeItem) => {
        if (!exchangeItem.ratable) {
          console.log(exchangeItem);
          this.toastr.info("This exchange is not ratable yet. Please wait three days or until all participants have marked it as completed.")
          this.closeRatingDialog();
        } else {
          this.showRatingForm = true;
        }
      },
      error: (error) => {
        this.toastr.error('Failed to load exchange status. Please try again.');
        console.error('Failed to load exchange status:', error.error.message);
      }
    });
  }

  private initForm(): void {
    this.ratingForm = this.fb.group({
      rating: [0, [Validators.required, Validators.min(0.5), Validators.max(5)]],
      title: ['', [Validators.required, Validators.maxLength(100)]],
      review: ['', [Validators.maxLength(500)]]
    });
  }

  setHoverRating(star: number, e: MouseEvent): void {
    const target = e.target as HTMLElement;
    const rect = target.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const percentage = mouseX / rect.width;

    // Use percentage to determine half or full star
    this.hoverRating = percentage <= 0.5 ? star - 0.5 : star;
  }

  setRating(star: number, e: MouseEvent): void {
    const target = e.target as HTMLElement;
    const rect = target.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const percentage = mouseX / rect.width;

    // Use percentage to determine half or full star
    this.currentRating = percentage <= 0.5 ? star - 0.5 : star;
    this.ratingForm.patchValue({ rating: this.currentRating });
  }

  isValid(): boolean {
    return this.ratingForm.valid && this.currentRating > 0;
  }

  onSubmit(): void {
    this.isSubmitted = true;

    if (this.isValid()) {
      const rating: CreateUserRating = {
        ratingHalfStars: this.currentRating * 2,
        title: this.ratingForm.get('title')?.value,
        description: this.ratingForm.get('review')?.value,
      };

      this.barteringService.createRatingForSkillExchange(this.exchangeId, rating).subscribe({
        next: (rating) => {
          this.toastr.success('Rating created successfully.');
          console.log("Rating created successfully.");
          this.closeRatingDialog();
        },
        error: (error) => {
          this.toastr.error('Failed to submit rating: ' + error.error.message);
          console.error('Rating submission error:', error.error.message);
        }
      });
    }
  }

  private closeRatingDialog() {
    this.ratingSubmitted.emit();  // This will now close the dialog
  }
}
