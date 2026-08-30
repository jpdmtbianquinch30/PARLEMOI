import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({
  selector: 'app-image-lightbox',
  standalone: true,
  templateUrl: './image-lightbox.html',
  styleUrl: './image-lightbox.scss'
})
export class ImageLightbox {
  @Input({ required: true }) url!: string;
  @Input() nom: string | null = null;
  @Output() fermer = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  surEchap(): void {
    this.fermer.emit();
  }
}