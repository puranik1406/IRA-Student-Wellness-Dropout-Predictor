package com.example.ira.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ira.ui.theme.*

@Composable
fun LandingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToStudentRegister: () -> Unit,
    onNavigateToCounselorRegister: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Navigation Bar
        TopNavBar(onNavigateToLogin)

        // Hero Section with Gradient Background
        HeroSection(onNavigateToLogin, onNavigateToStudentRegister, onNavigateToCounselorRegister)

        // Features Section
        FeaturesSection()

        // How It Works Section
        HowItWorksSection()

        // Founders Section
        FoundersSection()

        // Footer
        FooterSection()
    }
}

@Composable
fun TopNavBar(onNavigateToLogin: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo/Brand
            Text(
                text = "IRA",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Navy
            )

            // Login Button
            Button(
                onClick = onNavigateToLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Navy,
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Login",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun HeroSection(
    onNavigateToLogin: () -> Unit,
    onNavigateToStudentRegister: () -> Unit,
    onNavigateToCounselorRegister: () -> Unit
) {
    // Floating animation for the logo/icon
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(650.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Lavender.copy(alpha = 0.9f),
                        SkyBlue.copy(alpha = 0.9f),
                        MintGreen.copy(alpha = 0.9f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // App Title with fade-in animation
            Text(
                text = "Intuitive Reflection\nand Alert - IRA",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp
                ),
                color = Navy,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle
            Text(
                text = "An AI-powered platform that identifies at-risk students and provides personalized wellness support to prevent dropouts.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp
                ),
                color = Navy.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = onNavigateToStudentRegister,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        "Student Sign Up",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToCounselorRegister,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Navy
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Navy),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        "Counselor Sign Up",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Already have account link
            TextButton(
                onClick = onNavigateToLogin
            ) {
                Text(
                    "Already have an account? Log in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Navy
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                StatItem("85%", "Success Rate")
                StatItem("24/7", "AI Support")
                StatItem("100+", "Students")
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Navy
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = Navy.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun FeaturesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGray)
            .padding(24.dp)
    ) {
        Text(
            text = "Comprehensive Student Support",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Navy,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Everything students and counselors need in one platform",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Features Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(
                emoji = "📊",
                title = "Risk Assessment",
                description = "AI algorithms analyze attendance, grades, and mental health to identify at-risk students early."
            )

            FeatureCard(
                emoji = "😊",
                title = "Mood Tracking",
                description = "Daily check-ins help students monitor their mental well-being."
            )

            FeatureCard(
                emoji = "💬",
                title = "AI Chatbot",
                description = "24/7 AI companion provides immediate support and guidance."
            )

            FeatureCard(
                emoji = "📝",
                title = "Digital Journaling",
                description = "Private space for students to express thoughts and emotions."
            )
        }
    }
}

@Composable
fun FeatureCard(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emoji Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Lavender, SkyBlue)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 32.sp
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Navy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun HowItWorksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MintGreen.copy(alpha = 0.7f),
                        Lavender.copy(alpha = 0.7f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Text(
            text = "How IRA Works",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Navy,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A simple, effective approach to student wellness",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Steps
        StepCard(
            1,
            "Data Collection",
            "System gathers attendance, academic performance, and wellness data."
        )
        Spacer(modifier = Modifier.height(16.dp))

        StepCard(2, "AI Analysis", "Algorithms analyze patterns and calculate dropout risk scores.")
        Spacer(modifier = Modifier.height(16.dp))

        StepCard(3, "Early Intervention", "Counselors are alerted about at-risk students.")
        Spacer(modifier = Modifier.height(16.dp))

        StepCard(
            4,
            "Continuous Support",
            "Ongoing monitoring and personalized wellness recommendations."
        )
    }
}

@Composable
fun StepCard(number: Int, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number Circle
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = Navy,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Navy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun FoundersSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = "Meet Our Founders",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Navy,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Passionate students dedicated to improving educational outcomes",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Founders Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FounderCard(
                name = "Ishita Puranik",
                email = "ishitapurk14@gmail.com"
            )

            FounderCard(
                name = "Spoorthi Chava",
                email = "spoorthichava06@gmail.com"
            )

            FounderCard(
                name = "Mahek Muskaan Shaik",
                email = "mahekm.shaik@gmail.com"
            )

            FounderCard(
                name = "Geethanjali Bathini",
                email = "geethanjalibathini7@gmail.com"
            )
        }
    }
}

@Composable
fun FounderCard(name: String, email: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkyBlue.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Lavender, MintGreen)
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👤",
                    fontSize = 40.sp
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Navy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Co-Founder",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✉️ $email",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkyBlue
                )
            }
        }
    }
}

@Composable
fun FooterSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "© 2025 IRA - Intuitive Reflection and Alert",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Built with ❤️ for student success",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
