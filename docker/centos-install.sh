sudo su -
sudo yum -y install git
sudo yum -y install zsh
sudo sh -c "$(curl -fsSL https://raw.github.com/robbyrussell/oh-my-zsh/master/tools/install.sh)"
sed -i "/ZSH_THEME=/c\ZSH_THEME=\"agnoster\"" ~/.zshrc

git clone https://github.com/zsh-users/zsh-syntax-highlighting.git ${ZSH_CUSTOM:-~/.oh-my-zsh/custom}/plugins/zsh-syntax-highlighting
git clone https://github.com/zsh-users/zsh-autosuggestions ${ZSH_CUSTOM:-~/.oh-my-zsh/custom}/plugins/zsh-autosuggestions

sudo sed -i "/ZSH_THEME=/c\ZSH_THEME=\"agnoster\"" ~/.zshrc
sudo sed -i "/plugins=(git)/c\plugins=(\n\tgit\n\tzsh-syntax-highlighting\n\tzsh-autosuggestions\n)" ~/.zshrc

source ~/.zshrc

# sudo sh -c "$(curl -fsSL https://raw.githubusercontent.com/chk386/kafka-connect-playground/master/docker/centos-install.sh)"


# ssh -i notifications.pem centos@133.186.229.150
