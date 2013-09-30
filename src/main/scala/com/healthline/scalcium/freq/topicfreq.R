library(ggplot2)
df <- read.csv("topicfreq_3.csv", sep="\t", header=FALSE)
names(df) <- c("CFN", "PCNT")
df1 <- transform(df, CFN.lvl=reorder(CFN, PCNT))
ggplot(df1, aes(x=CFN.lvl, y=PCNT)) + 
  geom_bar(width=0.5, color="blue", stat="identity") + 
  coord_flip() + 
  labs(title="Percentage Composition of Topic Parents in HL CMS", 
       x="Concepts", y="Percentage Composition")
