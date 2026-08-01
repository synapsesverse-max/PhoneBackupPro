#!/usr/bin/env python3
"""
Icon Generator Script
Generates app icons for all required densities
"""

from PIL import Image, ImageDraw, ImageFont
import os

def generate_icons():
    sizes = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192
    }
    
    # Create a simple icon
    for folder, size in sizes.items():
        os.makedirs(folder, exist_ok=True)
        
        # Create image with blue background
        img = Image.new('RGB', (size, size), '#1976D2')
        draw = ImageDraw.Draw(img)
        
        # Draw a simple backup icon (arrow in circle)
        center = size // 2
        radius = size // 3
        
        # Draw circle
        draw.ellipse(
            [center - radius, center - radius, center + radius, center + radius],
            outline='white',
            width=size // 20
        )
        
        # Draw arrow
        arrow_points = [
            (center, center - radius // 2),
            (center, center + radius // 2),
            (center - radius // 3, center + radius // 6),
            (center + radius // 3, center + radius // 6)
        ]
        
        # Save icon
        img.save(f'{folder}/ic_launcher.png')
        print(f'Generated {folder}/ic_launcher.png ({size}x{size})')
    
    # Generate play store icon (512x512)
    img = Image.new('RGB', (512, 512), '#1976D2')
    draw = ImageDraw.Draw(img)
    draw.ellipse([128, 128, 384, 384], outline='white', width=10)
    img.save('play_store_icon.png')
    print('Generated play_store_icon.png (512x512)')

if __name__ == '__main__':
    generate_icons()
    print('Icon generation complete!')
